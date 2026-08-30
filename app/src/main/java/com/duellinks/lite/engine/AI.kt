package com.duellinks.lite.engine

import com.duellinks.lite.engine.CardType.*
import com.duellinks.lite.engine.Phase.*

object AiController {

    fun playTurn(engine: DuelEngine, player: Int) {
        var guard = 0
        while (engine.state.turn == player && engine.state.winner == -1 && guard < 80) {
            guard++
            when (engine.state.phase) {
                MAIN1 -> if (!trySummon(engine, player) && !trySpell(engine, player) && !trySet(engine, player)) {
                    engine.apply(NextPhaseAction(player))
                }
                BATTLE -> if (!tryAttack(engine, player)) engine.apply(NextPhaseAction(player))
                MAIN2 -> engine.apply(NextPhaseAction(player))
                else -> engine.apply(NextPhaseAction(player))
            }
        }
    }

    private fun trySummon(engine: DuelEngine, player: Int): Boolean {
        if (!engine.canNormalSummon(player)) return false
        val zone = engine.emptyMonsterZone(player) ?: return false
        val hand = engine.state.players[player].hand
        val monsters = hand.filter { it.type == MONSTER }.sortedByDescending { it.monster?.atk ?: 0 }
        for (c in monsters) {
            val lvl = c.monster?.level ?: 4
            val idx = hand.indexOf(c)
            if (lvl < 5) {
                engine.apply(SummonNormalAction(player, idx, zone, set = false))
                return true
            }
            val need = if (lvl >= 7) 2 else 1
            val tributes = (0..4).filter {
                val m = engine.state.monsterZones[player][it]
                m != null && m.owner == player
            }.take(need)
            if (tributes.size == need) {
                engine.apply(SummonTributeAction(player, idx, zone, tributes, set = false))
                return true
            }
        }
        return false
    }

    private fun trySpell(engine: DuelEngine, player: Int): Boolean {
        val zone = engine.emptySpellZone(player) ?: return false
        val hand = engine.state.players[player].hand
        val idx = hand.indexOfFirst { it.type == SPELL && it.effectTags.isNotEmpty() }
        if (idx < 0) return false
        engine.apply(ActivateSpellAction(player, idx, zone))
        return true
    }

    private fun trySet(engine: DuelEngine, player: Int): Boolean {
        val zone = engine.emptySpellZone(player) ?: return false
        val hand = engine.state.players[player].hand
        val idx = hand.indexOfFirst { it.type == SPELL || it.type == TRAP }
        if (idx < 0) return false
        val card = hand[idx]
        if (card.type == SPELL) engine.apply(SetSpellAction(player, idx, zone))
        else engine.apply(SetTrapAction(player, idx, zone))
        return true
    }

    private fun tryAttack(engine: DuelEngine, player: Int): Boolean {
        val st = engine.state
        val enemy = player xor 1
        val atkZone = (0..4).firstOrNull { st.monsterZones[player][it]?.canAttack == true } ?: return false
        val aAtk = st.monsterZones[player][atkZone]!!.card.monster?.atk ?: 0
        var target = -1
        var best = Int.MAX_VALUE
        for (z in 0..4) {
            val m = st.monsterZones[enemy][z] ?: continue
            val eff = if (m.position == Position.ATTACK) m.card.monster?.atk ?: 0 else m.card.monster?.def ?: 0
            if (eff < aAtk && eff < best) { best = eff; target = z }
        }
        return if (target >= 0) {
            engine.apply(AttackAction(player, atkZone, enemy, target))
            true
        } else if ((0..4).none { st.monsterZones[enemy][it] != null }) {
            engine.apply(AttackAction(player, atkZone, enemy, -1))
            true
        } else false
    }
}
