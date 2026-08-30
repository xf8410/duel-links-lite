package com.duellinks.lite.engine

import kotlin.math.max
import kotlin.random.Random

class DuelEngine(var state: GameState = GameState()) {

    private fun pName(p: Int) = if (p == 0) "玩家1" else "玩家2"

    private fun log(msg: String) {
        state = state.copy(log = (state.log + msg).takeLast(60))
    }

    private fun replacePlayer(i: Int, ps: PlayerState) {
        val list = state.players.toMutableList()
        list[i] = ps
        state = state.copy(players = list)
    }

    private fun placeMonster(owner: Int, zone: Int, card: Card, pos: Position, summonedThisTurn: Boolean, overlay: List<Card> = emptyList()) {
        val fm = FieldMonster(card, pos, owner, canAttack = true, changedThisTurn = false, summonedThisTurn = summonedThisTurn, overlay = overlay)
        state = state.copy(monsterZones = state.monsterZones.toMutableList().also { it[owner] = it[owner].toMutableList().also { zz -> zz[zone] = fm } })
    }

    private fun setMonsterPos(owner: Int, zone: Int, pos: Position, changedThisTurn: Boolean, canAttack: Boolean) {
        val m = state.monsterZones[owner][zone] ?: return
        val nm = m.copy(position = pos, changedThisTurn = changedThisTurn, canAttack = canAttack)
        state = state.copy(monsterZones = state.monsterZones.toMutableList().also { it[owner] = it[owner].toMutableList().also { zz -> zz[zone] = nm } })
    }

    private fun placeSpell(owner: Int, zone: Int, card: Card, faceUp: Boolean) {
        val fs = FieldSpellTrap(card, faceUp, owner)
        state = state.copy(spellZones = state.spellZones.toMutableList().also { it[owner] = it[owner].toMutableList().also { zz -> zz[zone] = fs } })
    }

    private fun sendSpellToGY(owner: Int, zone: Int) {
        val fs = state.spellZones[owner][zone] ?: return
        state = state.copy(spellZones = state.spellZones.toMutableList().also { it[owner] = it[owner].toMutableList().also { zz -> zz[zone] = null } })
        val ps = state.players[owner]
        replacePlayer(owner, ps.copy(graveyard = ps.graveyard + fs.card))
    }

    private fun burn(p: Int, amount: Int) {
        val ps = state.players[p]
        replacePlayer(p, ps.copy(lp = maxOf(0, ps.lp - amount)))
        log("${pName(p)} 受到 $amount 点伤害")
    }

    private fun heal(p: Int, amount: Int) {
        val ps = state.players[p]
        replacePlayer(p, ps.copy(lp = ps.lp + amount))
        log("${pName(p)} 回复 $amount LP")
    }

    private fun destroyMonster(player: Int, zone: Int) {
        val m = state.monsterZones[player][zone] ?: return
        state = state.copy(monsterZones = state.monsterZones.toMutableList().also { it[player] = it[player].toMutableList().also { zz -> zz[zone] = null } })
        val ps = state.players[player]
        replacePlayer(player, ps.copy(graveyard = ps.graveyard + m.card))
        log("${m.card.name} 被破坏")
    }

    private fun destroyAllMonsters() {
        for (p in 0..1) for (z in 0..4) {
            if (state.monsterZones[p][z] != null) destroyMonster(p, z)
        }
    }

    private fun destroyAllAttackMonsters(p: Int) {
        for (z in 0..4) {
            val m = state.monsterZones[p][z]
            if (m != null && m.position == Position.ATTACK) destroyMonster(p, z)
        }
    }

    private fun popSpellTrap(enemy: Int) {
        for (z in 0..4) {
            val fs = state.spellZones[enemy][z]
            if (fs != null && fs.faceUp) { sendSpellToGY(enemy, z); return }
        }
    }

    private fun revive(player: Int) {
        for (src in 0..1) {
            val gy = state.players[src].graveyard
            val idx = gy.indexOfFirst { it.type == CardType.MONSTER }
            if (idx >= 0) {
                val card = gy[idx]
                val zone = (0..4).firstOrNull { state.monsterZones[player][it] == null } ?: return
                val ps = state.players[src]
                replacePlayer(src, ps.copy(graveyard = ps.graveyard.toMutableList().also { it.removeAt(idx) }))
                placeMonster(player, zone, card, Position.ATTACK, summonedThisTurn = false)
                log("${pName(player)} 复活 ${card.name}")
                return
            }
        }
    }

    private fun waterOnlyOk(card: Card): Boolean {
        if ("WATER_ONLY_SUMMON" !in state.skillFlags) return true
        return card.monster?.attribute == Attribute.WATER
    }

    fun startGame(deck0: List<Card>, deck1: List<Card>, extra0: List<Card>, extra1: List<Card>) {
        val rnd = Random.Default
        val p0 = PlayerState(lp = 4000, deck = deck0.shuffled(rnd), extraDeck = extra0)
        val p1 = PlayerState(lp = 4000, deck = deck1.shuffled(rnd), extraDeck = extra1)
        state = GameState(
            players = listOf(p0, p1),
            monsterZones = List(2) { List(5) { null } },
            spellZones = List(2) { List(5) { null } },
            turn = 0, phase = Phase.MAIN1, turnCount = 1,
            normalSummonUsed = listOf(false, false),
            log = listOf("==== ${pName(0)} 先攻 ====")
        )
        repeat(4) { doDraw(0) }
        repeat(4) { doDraw(1) }
    }

    fun apply(action: Action): GameState {
        if (state.winner != -1) return state
        when (action) {
            is DrawAction -> doDraw(action.player)
            is SummonNormalAction -> doNormalSummon(action)
            is SummonTributeAction -> doTributeSummon(action)
            is ActivateSpellAction -> doActivateSpell(action)
            is SetSpellAction -> doSetSpell(action)
            is SetTrapAction -> doSetTrap(action)
            is ActivateTrapAction -> doActivateTrap(action)
            is ChangePositionAction -> doChangePosition(action)
            is AttackAction -> doAttack(action)
            is SpecialSummonAction -> doSpecialSummon(action)
            is NextPhaseAction -> doNextPhase(action.player)
            is SurrenderAction -> { state = state.copy(winner = action.player xor 1); log("${pName(action.player)} 投降") }
        }
        checkWin()
        return state
    }

    private fun doDraw(p: Int) {
        val ps = state.players[p]
        if (ps.deck.isEmpty()) { state = state.copy(winner = p xor 1); log("${pName(p)} 卡组耗尽，落败"); return }
        val card = ps.deck.first()
        replacePlayer(p, ps.copy(deck = ps.deck.drop(1), hand = ps.hand + card))
        log("${pName(p)} 抽卡")
    }

    private fun doNormalSummon(a: SummonNormalAction) {
        if (state.normalSummonUsed[a.player]) return
        val ps = state.players[a.player]
        val card = ps.hand.getOrNull(a.handIndex) ?: return
        if (card.type != CardType.MONSTER) return
        val lvl = card.monster?.level ?: 4
        if (lvl >= 5) return
        if (state.monsterZones[a.player][a.zone] != null) return
        if (!waterOnlyOk(card)) { log("${pName(a.player)} 技能限制：只能召唤水属性怪兽"); return }
        val pos = if (a.set) Position.DEFENSE_FACEDOWN else Position.ATTACK
        placeMonster(a.player, a.zone, card, pos, summonedThisTurn = true)
        val newHand = ps.hand.toMutableList().also { it.removeAt(a.handIndex) }
        replacePlayer(a.player, state.players[a.player].copy(hand = newHand))
        val nu = state.normalSummonUsed.toMutableList().also { it[a.player] = true }
        state = state.copy(normalSummonUsed = nu)
        log("${pName(a.player)} 通常召唤 ${card.name}")
    }

    private fun doTributeSummon(a: SummonTributeAction) {
        if (state.normalSummonUsed[a.player]) return
        val ps = state.players[a.player]
        val card = ps.hand.getOrNull(a.handIndex) ?: return
        if (card.type != CardType.MONSTER) return
        val lvl = card.monster?.level ?: 4
        val need = when { lvl >= 7 -> 2; lvl >= 5 -> 1; else -> 0 }
        if (a.tributes.size != need) return
        if (state.monsterZones[a.player][a.zone] != null) return
        if (!waterOnlyOk(card)) { log("${pName(a.player)} 技能限制：只能召唤水属性怪兽"); return }
        for (z in a.tributes) {
            val m = state.monsterZones[a.player][z]
            if (m == null || m.owner != a.player) return
        }
        val sent = mutableListOf<Card>()
        val pz = state.monsterZones[a.player].toMutableList()
        for (z in a.tributes) { pz[z]?.let { sent.add(it.card) }; pz[z] = null }
        state = state.copy(monsterZones = state.monsterZones.toMutableList().also { it[a.player] = pz })
        val newHand = ps.hand.toMutableList().also { it.removeAt(a.handIndex) }
        val ps2 = state.players[a.player].copy(hand = newHand, graveyard = state.players[a.player].graveyard + sent)
        replacePlayer(a.player, ps2)
        val pos = if (a.set) Position.DEFENSE_FACEDOWN else Position.ATTACK
        placeMonster(a.player, a.zone, card, pos, summonedThisTurn = true)
        state = state.copy(normalSummonUsed = state.normalSummonUsed.toMutableList().also { it[a.player] = true })
        log("${pName(a.player)} 上级召唤 ${card.name}")
    }

    private fun doActivateSpell(a: ActivateSpellAction) {
        val ps = state.players[a.player]
        val card = ps.hand.getOrNull(a.handIndex) ?: return
        if (card.type != CardType.SPELL) return
        if (state.spellZones[a.player][a.zone] != null) return
        val newHand = ps.hand.toMutableList().also { it.removeAt(a.handIndex) }
        replacePlayer(a.player, state.players[a.player].copy(hand = newHand))
        placeSpell(a.player, a.zone, card, faceUp = true)
        log("${pName(a.player)} 发动 ${card.name}")
        resolveSpell(a.player, card)
        if (card.spellType == SpellType.NORMAL) sendSpellToGY(a.player, a.zone)
    }

    private fun doSetSpell(a: SetSpellAction) {
        val ps = state.players[a.player]
        val card = ps.hand.getOrNull(a.handIndex) ?: return
        if (card.type != CardType.SPELL) return
        if (state.spellZones[a.player][a.zone] != null) return
        val newHand = ps.hand.toMutableList().also { it.removeAt(a.handIndex) }
        replacePlayer(a.player, state.players[a.player].copy(hand = newHand))
        placeSpell(a.player, a.zone, card, faceUp = false)
        log("${pName(a.player)} 盖放 ${card.name}")
    }

    private fun doSetTrap(a: SetTrapAction) {
        val ps = state.players[a.player]
        val card = ps.hand.getOrNull(a.handIndex) ?: return
        if (card.type != CardType.TRAP) return
        if (state.spellZones[a.player][a.zone] != null) return
        val newHand = ps.hand.toMutableList().also { it.removeAt(a.handIndex) }
        replacePlayer(a.player, state.players[a.player].copy(hand = newHand))
        placeSpell(a.player, a.zone, card, faceUp = false)
        log("${pName(a.player)} 盖放 ${card.name}")
    }

    private fun doActivateTrap(a: ActivateTrapAction) {
        val fs = state.spellZones[a.player][a.zone] ?: return
        if (fs.faceUp || fs.card.type != CardType.TRAP) return
        state = state.copy(spellZones = state.spellZones.toMutableList().also {
            it[a.player] = it[a.player].toMutableList().also { zz -> zz[a.zone] = zz[a.zone]!!.copy(faceUp = true) }
        })
        log("${pName(a.player)} 发动陷阱 ${fs.card.name}")
        sendSpellToGY(a.player, a.zone)
    }

    private fun resolveSpell(player: Int, card: Card) {
        for (tag in card.effectTags) {
            when (tag) {
                EffectTag.DRAW_2 -> { doDraw(player); doDraw(player) }
                EffectTag.HEAL_1000 -> heal(player, 1000)
                EffectTag.DESTROY_ALL_MONSTERS -> destroyAllMonsters()
                EffectTag.DESTROY_ONE_MONSTER -> {
                    val enemy = 1 - player
                    for (z in 0..4) if (state.monsterZones[enemy][z] != null) { destroyMonster(enemy, z); break }
                }
                EffectTag.BURN_500 -> burn(1 - player, 500)
                EffectTag.BUFF_SELF_500 -> {
                    for (z in 0..4) {
                        val m = state.monsterZones[player][z] ?: continue
                        val st = m.card.monster ?: continue
                        val boosted = m.card.copy(monster = st.copy(atk = st.atk + 500))
                        state = state.copy(monsterZones = state.monsterZones.toMutableList().also { it[player] = it[player].toMutableList().also { zz -> zz[z] = m.copy(card = boosted) } })
                    }
                }
                EffectTag.POP_SPELL_TRAP -> popSpellTrap(1 - player)
                EffectTag.REVIVE_ONE -> revive(player)
            }
        }
    }

    private fun doChangePosition(a: ChangePositionAction) {
        val m = state.monsterZones[a.player][a.zone] ?: return
        if (m.changedThisTurn) return
        if (m.position == Position.ATTACK) setMonsterPos(a.player, a.zone, Position.DEFENSE_FACEUP, changedThisTurn = true, canAttack = false)
        else setMonsterPos(a.player, a.zone, Position.ATTACK, changedThisTurn = true, canAttack = true)
        log("${pName(a.player)} 变更 ${m.card.name} 表示形式")
    }

    private fun doAttack(a: AttackAction) {
        val attacker = state.monsterZones[a.player][a.attackerZone] ?: return
        if (!attacker.canAttack) return
        triggerOpponentTraps(a.player, a)
        if (state.winner != -1) return
        val atk = state.monsterZones[a.player][a.attackerZone] ?: return
        val aAtk = atk.card.monster?.atk ?: 0
        if (a.targetZone == -1) {
            if (hasFaceUpMonster(a.targetPlayer)) return
            burn(a.targetPlayer, aAtk)
        } else {
            val def = state.monsterZones[a.targetPlayer][a.targetZone]
            if (def == null) burn(a.targetPlayer, aAtk)
            else resolveBattle(a.player, a.targetPlayer, a.attackerZone, a.targetZone)
        }
        state = state.copy(monsterZones = state.monsterZones.toMutableList().also { it[a.player] = it[a.player].toMutableList().also { zz -> zz[a.attackerZone] = zz[a.attackerZone]!!.copy(canAttack = false) } })
        checkWin()
    }

    private fun hasFaceUpMonster(p: Int) = (0..4).any { state.monsterZones[p][it] != null }

    private fun resolveBattle(atkP: Int, defP: Int, atkZ: Int, defZ: Int) {
        val atk = state.monsterZones[atkP][atkZ]!!
        val def = state.monsterZones[defP][defZ]!!
        val a = atk.card.monster!!.atk
        when (def.position) {
            Position.ATTACK -> {
                val d = def.card.monster!!.atk
                when {
                    a > d -> { destroyMonster(defP, defZ); burn(defP, a - d) }
                    a < d -> { destroyMonster(atkP, atkZ); burn(atkP, d - a) }
                    else -> { destroyMonster(atkP, atkZ); destroyMonster(defP, defZ) }
                }
            }
            else -> {
                val d = def.card.monster!!.def ?: 0
                when {
                    a > d -> destroyMonster(defP, defZ)
                    a < d -> burn(atkP, d - a)
                }
            }
        }
    }

    private fun triggerOpponentTraps(atkPlayer: Int, a: AttackAction) {
        val defP = 1 - atkPlayer
        for (z in 0..4) {
            val fs = state.spellZones[defP][z] ?: continue
            if (fs.faceUp || fs.card.type != CardType.TRAP) continue
            when (fs.card.name) {
                "神圣防护罩-反射镜力-" -> {
                    destroyAllAttackMonsters(atkPlayer)
                    sendSpellToGY(defP, z)
                    log("镜壁发动！")
                }
                "落穴" -> {
                    val atk = state.monsterZones[a.player][a.attackerZone] ?: return
                    if ((atk.card.monster?.atk ?: 0) >= 1000) {
                        destroyMonster(a.player, a.attackerZone)
                        sendSpellToGY(defP, z)
                        log("落穴发动！")
                    }
                }
            }
        }
    }

    private fun gatherMaterials(player: Int, handIdx: List<Int>, fieldZones: List<Int>): List<Card> {
        val ps = state.players[player]
        val cards = mutableListOf<Card>()
        for (i in handIdx) ps.hand.getOrNull(i)?.let { cards.add(it) }
        for (z in fieldZones) state.monsterZones[player][z]?.let { cards.add(it.card) }
        return cards
    }

    private fun removeMaterials(player: Int, handIdx: List<Int>, fieldZones: List<Int>) {
        val ps = state.players[player]
        val newHand = ps.hand.toMutableList().apply { handIdx.sortedDescending().forEach { if (it in indices) removeAt(it) } }
        replacePlayer(player, ps.copy(hand = newHand))
        val pz = state.monsterZones[player].toMutableList()
        for (z in fieldZones) pz[z] = null
        state = state.copy(monsterZones = state.monsterZones.toMutableList().also { it[player] = pz })
    }

    private fun popExtra(player: Int, index: Int) {
        state = state.copy(players = state.players.toMutableList().also {
            it[player] = it[player].copy(extraDeck = it[player].extraDeck.toMutableList().also { ex -> ex.removeAt(index) })
        })
    }

    private fun doSpecialSummon(a: SpecialSummonAction) {
        val ps = state.players[a.player]
        if (state.monsterZones[a.player][a.zone] != null) return
        val mats = gatherMaterials(a.player, a.materialHandIndices, a.materialFieldZones)
        when (a.kind) {
            SummonKind.FUSION -> {
                val extra = ps.extraDeck.getOrNull(a.fromExtraIndex) ?: return
                if (extra.kind != SummonKind.FUSION) return
                if (!waterOnlyOk(extra)) { log("${pName(a.player)} 技能限制：只能召唤水属性怪兽"); return }
                if (!matchFusion(extra, mats)) return
                removeMaterials(a.player, a.materialHandIndices, a.materialFieldZones)
                popExtra(a.player, a.fromExtraIndex)
                placeMonster(a.player, a.zone, extra, Position.ATTACK, summonedThisTurn = false)
                log("${pName(a.player)} 融合召唤 ${extra.name}")
            }
            SummonKind.SYNCHRO -> {
                val extra = ps.extraDeck.getOrNull(a.fromExtraIndex) ?: return
                if (extra.kind != SummonKind.SYNCHRO) return
                if (!waterOnlyOk(extra)) { log("${pName(a.player)} 技能限制：只能召唤水属性怪兽"); return }
                if (!matchSynchro(extra, mats)) return
                removeMaterials(a.player, a.materialHandIndices, a.materialFieldZones)
                popExtra(a.player, a.fromExtraIndex)
                placeMonster(a.player, a.zone, extra, Position.ATTACK, summonedThisTurn = false)
                log("${pName(a.player)} 同调召唤 ${extra.name}")
            }
            SummonKind.XYZ -> {
                val extra = ps.extraDeck.getOrNull(a.fromExtraIndex) ?: return
                if (extra.kind != SummonKind.XYZ) return
                if (!waterOnlyOk(extra)) { log("${pName(a.player)} 技能限制：只能召唤水属性怪兽"); return }
                if (!matchXyz(extra, mats)) return
                popExtra(a.player, a.fromExtraIndex)
                placeMonster(a.player, a.zone, extra, Position.ATTACK, summonedThisTurn = false, overlay = mats)
                removeMaterials(a.player, a.materialHandIndices, a.materialFieldZones)
                log("${pName(a.player)} 超量召唤 ${extra.name}")
            }
            SummonKind.LINK -> {
                val extra = ps.extraDeck.getOrNull(a.fromExtraIndex) ?: return
                if (extra.kind != SummonKind.LINK) return
                if (!waterOnlyOk(extra)) { log("${pName(a.player)} 技能限制：只能召唤水属性怪兽"); return }
                if (mats.isEmpty()) return
                removeMaterials(a.player, a.materialHandIndices, a.materialFieldZones)
                popExtra(a.player, a.fromExtraIndex)
                placeMonster(a.player, a.zone, extra, Position.ATTACK, summonedThisTurn = false)
                log("${pName(a.player)} 连接召唤 ${extra.name}")
            }
            SummonKind.PENDULUM -> {
                val scales = state.players[a.player].pendulumZone.mapNotNull { it.monster?.pendulumScale }
                if (scaled.size < 2) return
                val lo = scales.minOrNull()!!; val hi = scales.maxOrNull()!!
                val card = if (a.fromHandIndex >= 0) ps.hand.getOrNull(a.fromHandIndex) else null
                    ?: ps.extraDeck.getOrNull(a.fromExtraIndex)
                val lvl = card?.monster?.level ?: return
                if (lvl <= lo || lvl >= hi) return
                if (!waterOnlyOk(card)) { log("${pName(a.player)} 技能限制：只能召唤水属性怪兽"); return }
                if (a.fromHandIndex >= 0) {
                    val newHand = ps.hand.toMutableList().also { it.removeAt(a.fromHandIndex) }
                    replacePlayer(a.player, ps.copy(hand = newHand))
                } else {
                    popExtra(a.player, a.fromExtraIndex)
                }
                placeMonster(a.player, a.zone, card!!, Position.ATTACK, summonedThisTurn = true)
                log("${pName(a.player)} 灵摆召唤 ${card.name}")
            }
            else -> return
        }
    }

    private fun matchFusion(fusion: Card, mats: List<Card>): Boolean {
        val need = fusion.materials
        if (need.isEmpty()) return true
        val have = mats.map { it.name }.toMutableList()
        for (n in need) {
            val idx = have.indexOf(n)
            if (idx < 0) return false
            have.removeAt(idx)
        }
        return true
    }

    private fun matchSynchro(syn: Card, mats: List<Card>): Boolean {
        val lvl = syn.monster?.level ?: return false
        val tuners = mats.filter { it.monster?.tuner == true }
        val non = mats.filter { it.monster?.tuner != true }
        if (tuners.isEmpty() || non.isEmpty()) return false
        return mats.sumOf { it.monster?.level ?: 0 } == lvl
    }

    private fun matchXyz(xyz: Card, mats: List<Card>): Boolean {
        val rank = xyz.monster?.rank ?: return false
        if (mats.size < 2) return false
        return mats.all { (it.monster?.rank ?: it.monster?.level ?: 0) == rank }
    }

    private fun doNextPhase(player: Int) {
        if (player != state.turn) return
        state = when (state.phase) {
            Phase.DRAW -> state.copy(phase = Phase.MAIN1)
            Phase.MAIN1 -> if (state.turnCount == 1 && state.turn == 0) state.copy(phase = Phase.MAIN2) else state.copy(phase = Phase.BATTLE)
            Phase.BATTLE -> state.copy(phase = Phase.MAIN2)
            Phase.MAIN2 -> endTurn()
            Phase.END -> endTurn()
        }
    }

    private fun endTurn(): GameState {
        val next = state.turn xor 1
        val newCount = if (next == 0) state.turnCount + 1 else state.turnCount
        state = state.copy(turn = next, phase = Phase.DRAW, turnCount = newCount, normalSummonUsed = listOf(false, false))
        for (p in 0..1) for (z in 0..4) {
            val m = state.monsterZones[p][z] ?: continue
            state = state.copy(monsterZones = state.monsterZones.toMutableList().also { it[p] = it[p].toMutableList().also { zz -> zz[z] = m.copy(summonedThisTurn = false, changedThisTurn = false) } })
        }
        doDraw(next)
        log("==== ${pName(next)} 的回合 ====")
        return state
    }

    private fun checkWin() {
        for (i in 0..1) {
            if (state.players[i].lp <= 0) { state = state.copy(winner = i xor 1); log("${pName(i)} LP 归零，败北"); return }
        }
    }

    fun emptyMonsterZone(p: Int) = (0..4).firstOrNull { state.monsterZones[p][it] == null }
    fun emptySpellZone(p: Int) = (0..4).firstOrNull { state.spellZones[p][it] == null }
    fun canNormalSummon(p: Int) = !state.normalSummonUsed[p]
}
