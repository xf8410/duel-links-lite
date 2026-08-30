package com.duellinks.lite.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EngineTest {

    private fun fresh(): DuelEngine {
        val e = DuelEngine()
        e.startGame(
            CardDatabase.defaultDeck(), CardDatabase.defaultDeck(),
            CardDatabase.defaultExtraDeck(), CardDatabase.defaultExtraDeck()
        )
        return e
    }

    private fun setName(n: String) = CardDatabase.all.first { it.name == n }

    @Test
    fun normalSummonPlacesMonster() {
        val e = fresh()
        val alex = setName("亚历山大龙")
        e.state = e.state.copy(players = e.state.players.toMutableList().also {
            it[0] = it[0].copy(hand = listOf(alex), deck = emptyList())
        })
        e.apply(SummonNormalAction(0, 0, 0, false))
        assertNotNull(e.state.monsterZones[0][0])
        assertEquals("亚历山大龙", e.state.monsterZones[0][0]!!.card.name)
    }

    @Test
    fun battleDamageApplies() {
        val e = fresh()
        val alex = setName("亚历山大龙") // 2000 ATK
        val petit = setName("小妖龙")    // 1200 ATK
        e.state = e.state.copy(players = e.state.players.toMutableList().also {
            it[0] = it[0].copy(hand = listOf(alex), deck = emptyList())
            it[1] = it[1].copy(hand = listOf(petit), deck = emptyList())
        })
        e.apply(SummonNormalAction(0, 0, 0, false))
        e.apply(SummonNormalAction(1, 0, 0, false))
        e.apply(AttackAction(0, 0, 1, 0))
        assertNull(e.state.monsterZones[1][0])
        assertEquals(4000 - 800, e.state.players[1].lp)
    }

    @Test
    fun fusionConsumesMaterialsAndExtra() {
        val e = fresh()
        val bewd = setName("青眼白龙")
        val ultimate = setName("青眼究极龙")
        val extraBefore = e.state.players[0].extraDeck.size
        val extraIdx = e.state.players[0].extraDeck.indexOf(ultimate)
        e.state = e.state.copy(players = e.state.players.toMutableList().also {
            it[0] = it[0].copy(hand = listOf(bewd, bewd, bewd), deck = emptyList())
        })
        e.apply(
            SpecialSummonAction(
                0, SummonKind.FUSION, 0,
                fromExtraIndex = extraIdx, materialHandIndices = listOf(0, 1, 2)
            )
        )
        assertEquals(extraBefore - 1, e.state.players[0].extraDeck.size)
        assertEquals("青眼究极龙", e.state.monsterZones[0][0]!!.card.name)
    }

    @Test
    fun lpZeroDeclaresWinner() {
        val e = fresh()
        val alex = setName("亚历山大龙")
        val petit = setName("小妖龙")
        e.state = e.state.copy(players = e.state.players.toMutableList().also {
            it[0] = it[0].copy(hand = listOf(alex), deck = emptyList())
            it[1] = it[1].copy(hand = listOf(petit), deck = emptyList(), lp = 500)
        })
        e.apply(SummonNormalAction(0, 0, 0, false))
        e.apply(SummonNormalAction(1, 0, 0, false))
        e.apply(AttackAction(0, 0, 1, 0))
        assertEquals(0, e.state.players[1].lp)
        assertEquals(0, e.state.winner)
    }
}
