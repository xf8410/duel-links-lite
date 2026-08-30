package com.duellinks.lite.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AoiTest {

    @Test
    fun skillSetupAddsMissingExtraDeckCards() {
        val trickstarExtra = Aoi.deckFor(Skin.BLUE_ANGEL).second // 只有淘气仙星连接怪兽
        val skill = Aoi.skills.first { it.id == "new_possibility" } // 加海晶少女卡
        val after = Aoi.applySkillSetup(skill, trickstarExtra)
        assertTrue(after.any { it.name == "海晶少女 魔泡大堡垒" })
        assertTrue(after.any { it.name == "海晶少女 珊瑚海葵" })
    }

    @Test
    fun linkSummonPlacesIntoAnyMainZone() {
        val e = DuelEngine()
        val (deck, extra) = Aoi.deckFor(Skin.BLUE_ANGEL)
        e.startGame(deck, deck, extra, extra)
        val mat = deck.first { it.type == CardType.MONSTER }
        e.state = e.state.copy(players = e.state.players.toMutableList().also {
            it[0] = it[0].copy(hand = listOf(mat), deck = emptyList())
        })
        val extraIdx = e.state.players[0].extraDeck.indexOfFirst { it.kind == SummonKind.LINK }
        val linkName = e.state.players[0].extraDeck[extraIdx].name
        // 无额外怪兽区限制：放到主怪兽区 3（前5后5，0..4 都是主怪兽区）
        e.apply(SpecialSummonAction(0, SummonKind.LINK, 3, fromExtraIndex = extraIdx, materialHandIndices = listOf(0)))
        assertEquals(linkName, e.state.monsterZones[0][3]?.card?.name)
    }
}
