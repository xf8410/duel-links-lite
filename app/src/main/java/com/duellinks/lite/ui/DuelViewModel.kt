package com.duellinks.lite.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.duellinks.lite.engine.*

enum class Mode { AI, HOTSEAT, NET }

class DuelViewModel : ViewModel() {

    var mode = Mode.AI
    var viewPlayer = 0
    var localPlayer = 0
    var engine = DuelEngine()
    var state = mutableStateOf(engine.state)
    var selectedHand = mutableStateOf(-1)
    var selectedMonster = mutableStateOf(-1)
    var attackMode = mutableStateOf(false)
    var showLog = mutableStateOf(false)
    var menuScreen = mutableStateOf(true)
    var passOverlay = mutableStateOf(false)
    var passTo = mutableStateOf(0)
    var netError = mutableStateOf("")

    // 财前葵形态与技能（技能互通）
    var useAoi = mutableStateOf(false)
    var selectedSkin = mutableStateOf(Skin.BLUE_ANGEL)
    var selectedSkill = mutableStateOf(Aoi.skills.first())

    private var net: LanConnection? = null

    private fun deckForPlayer0(): Pair<List<Card>, List<Card>> =
        if (useAoi.value) Aoi.deckFor(selectedSkin.value) else (CardDatabase.defaultDeck() to CardDatabase.defaultExtraDeck())

    fun start(mode: Mode) {
        this.mode = mode
        engine = DuelEngine()
        val (d0, e0) = deckForPlayer0()
        engine.startGame(d0, CardDatabase.defaultDeck(), e0, CardDatabase.defaultExtraDeck())
        if (useAoi.value) applySkillSetup()
        viewPlayer = 0
        localPlayer = 0
        clearSelection()
        menuScreen.value = false
        passOverlay.value = false
        state.value = engine.state
        if (mode == Mode.AI) maybeRunAi()
    }

    private fun applySkillSetup() {
        val extra = Aoi.applySkillSetup(selectedSkill.value, engine.state.players[0].extraDeck)
        engine.state = engine.state.copy(players = engine.state.players.toMutableList().also { it[0] = it[0].copy(extraDeck = extra) })
    }

    fun startNet(isHost: Boolean, host: String = "", port: Int = 8765) {
        net = LanConnection()
        localPlayer = if (isHost) 0 else 1
        viewPlayer = localPlayer
        net!!.onState = { s ->
            engine.state = s
            state.value = s
        }
        net!!.onAction = { a ->
            if (localPlayer == 0) {
                engine.apply(a)
                state.value = engine.state
                net!!.sendState(engine.state)
                maybeRunAi()
            }
        }
        net!!.onConnected = {
            if (isHost) {
                engine = DuelEngine()
                val (d0, e0) = deckForPlayer0()
                engine.startGame(d0, CardDatabase.defaultDeck(), e0, CardDatabase.defaultExtraDeck())
                if (useAoi.value) applySkillSetup()
                state.value = engine.state
                net!!.sendState(engine.state)
            }
        }
        net!!.onError = { netError.value = it }
        if (isHost) net!!.startHost(port) else net!!.connect(host, port)
        menuScreen.value = false
        clearSelection()
    }

    fun isMyTurn(): Boolean {
        if (state.value.winner != -1) return false
        return when (mode) {
            Mode.AI -> state.value.turn == 0
            Mode.HOTSEAT -> state.value.turn == viewPlayer
            Mode.NET -> state.value.turn == localPlayer
        }
    }

    fun isInteractive(): Boolean = isMyTurn() && !passOverlay.value

    fun perform(action: Action) {
        if (!isMyTurn()) return
        if (mode == Mode.NET) {
            net?.sendAction(action)
            if (localPlayer == 0) {
                engine.apply(action)
                state.value = engine.state
                net!!.sendState(engine.state)
                maybeRunAi()
            }
        } else {
            engine.apply(action)
            state.value = engine.state
            if (mode == Mode.AI) maybeRunAi()
        }
        clearSelection()
        if (mode == Mode.HOTSEAT && state.value.turn != viewPlayer && state.value.winner == -1) {
            passOverlay.value = true
            passTo.value = state.value.turn
        }
    }

    fun dismissPass() {
        viewPlayer = passTo.value
        passOverlay.value = false
    }

    private fun maybeRunAi() {
        if (mode != Mode.AI || state.value.winner != -1) return
        if (state.value.turn == 1) {
            Thread {
                AiController.playTurn(engine, 1)
                state.value = engine.state
            }.start()
        }
    }

    fun clearSelection() {
        selectedHand.value = -1
        selectedMonster.value = -1
        attackMode.value = false
    }

    fun humanSummon(handIndex: Int, set: Boolean) {
        val st = engine.state
        val ps = st.players[viewPlayer]
        val card = ps.hand.getOrNull(handIndex) ?: return
        if (card.type != CardType.MONSTER) return
        val zone = engine.emptyMonsterZone(viewPlayer) ?: return
        val lvl = card.monster?.level ?: 4
        if (lvl < 5) {
            perform(SummonNormalAction(viewPlayer, handIndex, zone, set))
        } else {
            val need = if (lvl >= 7) 2 else 1
            val tributes = (0..4).filter { st.monsterZones[viewPlayer][it]?.owner == viewPlayer }.take(need)
            if (tributes.size == need) perform(SummonTributeAction(viewPlayer, handIndex, zone, tributes, set))
        }
    }

    fun humanActivateSpell(handIndex: Int) {
        val zone = engine.emptySpellZone(viewPlayer) ?: return
        perform(ActivateSpellAction(viewPlayer, handIndex, zone))
    }

    fun humanSet(handIndex: Int) {
        val zone = engine.emptySpellZone(viewPlayer) ?: return
        val card = engine.state.players[viewPlayer].hand.getOrNull(handIndex) ?: return
        if (card.type == CardType.SPELL) perform(SetSpellAction(viewPlayer, handIndex, zone))
        else perform(SetTrapAction(viewPlayer, handIndex, zone))
    }

    fun humanChangePosition(zone: Int) = perform(ChangePositionAction(viewPlayer, zone))
    fun humanAttack(attackerZone: Int, targetPlayer: Int, targetZone: Int) =
        perform(AttackAction(viewPlayer, attackerZone, targetPlayer, targetZone))

    fun humanNextPhase() = perform(NextPhaseAction(viewPlayer))
    fun humanSurrender() = perform(SurrenderAction(viewPlayer))

    fun autoSpecial(kind: SummonKind, extraIndex: Int, handIndex: Int, zone: Int): Boolean {
        val st = engine.state
        val ps = st.players[viewPlayer]
        val handMons = ps.hand.mapIndexedNotNull { i, c -> if (c.type == CardType.MONSTER) i else null }
        val fieldMons = (0..4).filter { st.monsterZones[viewPlayer][it] != null }
        return when (kind) {
            SummonKind.FUSION -> {
                val extra = ps.extraDeck.getOrNull(extraIndex) ?: return false
                val mats = pickFusion(extra, handMons, fieldMons) ?: return false
                perform(SpecialSummonAction(viewPlayer, kind, zone, fromExtraIndex = extraIndex, materialHandIndices = mats.first, materialFieldZones = mats.second))
                true
            }
            SummonKind.SYNCHRO -> {
                val extra = ps.extraDeck.getOrNull(extraIndex) ?: return false
                val mats = pickSynchro(extra, handMons, fieldMons) ?: return false
                perform(SpecialSummonAction(viewPlayer, kind, zone, fromExtraIndex = extraIndex, materialHandIndices = mats.first, materialFieldZones = mats.second))
                true
            }
            SummonKind.XYZ -> {
                val extra = ps.extraDeck.getOrNull(extraIndex) ?: return false
                val mats = pickXyz(extra, handMons, fieldMons) ?: return false
                perform(SpecialSummonAction(viewPlayer, kind, zone, fromExtraIndex = extraIndex, materialHandIndices = mats.first, materialFieldZones = mats.second))
                true
            }
            SummonKind.LINK -> {
                val extra = ps.extraDeck.getOrNull(extraIndex) ?: return false
                val mats = pickLink(handMons, fieldMons) ?: return false
                perform(SpecialSummonAction(viewPlayer, kind, zone, fromExtraIndex = extraIndex, materialHandIndices = mats.first, materialFieldZones = mats.second))
                true
            }
            SummonKind.PENDULUM -> {
                if (handIndex >= 0) perform(SpecialSummonAction(viewPlayer, kind, zone, fromHandIndex = handIndex))
                else perform(SpecialSummonAction(viewPlayer, kind, zone, fromExtraIndex = extraIndex))
                true
            }
            else -> false
        }
    }

    private fun pickFusion(extra: Card, handMons: List<Int>, fieldMons: List<Int>): Pair<List<Int>, List<Int>>? {
        val st = engine.state
        val need = extra.materials
        if (need.isEmpty()) return null
        val usedHand = mutableSetOf<Int>()
        val usedField = mutableSetOf<Int>()
        val handPicked = mutableListOf<Int>()
        val fieldPicked = mutableListOf<Int>()
        for (n in need) {
            var found = false
            for (i in handMons) {
                if (i in usedHand) continue
                if (st.players[viewPlayer].hand[i].name == n) { usedHand.add(i); handPicked.add(i); found = true; break }
            }
            if (!found) for (z in fieldMons) {
                if (z in usedField) continue
                if (st.monsterZones[viewPlayer][z]!!.card.name == n) { usedField.add(z); fieldPicked.add(z); found = true; break }
            }
            if (!found) return null
        }
        return handPicked to fieldPicked
    }

    private fun pickSynchro(extra: Card, handMons: List<Int>, fieldMons: List<Int>): Pair<List<Int>, List<Int>>? {
        val st = engine.state
        val lvl = extra.monster?.level ?: return null
        val cand = buildList {
            handMons.forEach { i -> add(Triple(true, i, st.players[viewPlayer].hand[i])) }
            fieldMons.forEach { z -> add(Triple(false, z, st.monsterZones[viewPlayer][z]!!.card)) }
        }
        for (a in cand) for (b in cand) if (a != b) {
            val tuners = listOf(a, b).count { it.third.monster?.tuner == true }
            if (tuners == 1 && listOf(a, b).sumOf { it.third.monster?.level ?: 0 } == lvl) {
                val h = listOf(a, b).filter { it.first }.map { it.second }
                val f = listOf(a, b).filter { !it.first }.map { it.second }
                return h to f
            }
        }
        return null
    }

    private fun pickXyz(extra: Card, handMons: List<Int>, fieldMons: List<Int>): Pair<List<Int>, List<Int>>? {
        val st = engine.state
        val rank = extra.monster?.rank ?: return null
        val cand = buildList {
            handMons.forEach { i -> add(Triple(true, i, st.players[viewPlayer].hand[i])) }
            fieldMons.forEach { z -> add(Triple(false, z, st.monsterZones[viewPlayer][z]!!.card)) }
        }
        for (a in cand) for (b in cand) if (a != b) {
            val ra = a.third.monster?.rank ?: a.third.monster?.level ?: 0
            val rb = b.third.monster?.rank ?: b.third.monster?.level ?: 0
            if (ra == rank && rb == rank) {
                val h = listOf(a, b).filter { it.first }.map { it.second }
                val f = listOf(a, b).filter { !it.first }.map { it.second }
                return h to f
            }
        }
        return null
    }

    private fun pickLink(handMons: List<Int>, fieldMons: List<Int>): Pair<List<Int>, List<Int>>? {
        val all = handMons.map { true to it } + fieldMons.map { false to it }
        if (all.isEmpty()) return null
        val (isHand, idx) = all.first()
        return if (isHand) listOf(idx) to emptyList<Int>() else emptyList<Int>() to listOf(idx)
    }
}
