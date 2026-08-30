package com.duellinks.lite.engine

import com.duellinks.lite.engine.Attribute.*
import com.duellinks.lite.engine.Race.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AoiTest {

    @Test
    fun skillSetupAddsMissingExtraDeckCards() {
        val trickstarExtra = Aoi.deckFor(Skin.BLUE_ANGEL).second
        val skill = Aoi.skills.first { it.id == "new_possibility" }
        val after = Aoi.applySkillSetup(skill, trickstarExtra)
        assertTrue(after.any { it.name == "Marincess Great Bubble Reef" })
        assertTrue(after.any { it.name == "Marincess Coral Anemone" })
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
        e.apply(SpecialSummonAction(0, SummonKind.LINK, 3, fromExtraIndex = extraIdx, materialHandIndices = listOf(0)))
        assertEquals(linkName, e.state.monsterZones[0][3]?.card?.name)
    }

    @Test
    fun trickstarCardsHaveCorrectStats() {
        val candina = Aoi.byName("Trickstar Candina")!!
        assertEquals(1800, candina.monster?.atk)
        assertEquals(400, candina.monster?.def)
        assertEquals(4, candina.monster?.level)
        assertEquals(LIGHT, candina.monster?.attribute)
        assertEquals(FAIRY, candina.monster?.race)

        val holly = Aoi.byName("Trickstar Holly Angel")!!
        assertEquals(2000, holly.monster?.atk)
        assertEquals(2, holly.monster?.link)
        assertEquals(listOf(LinkArrow.BOTTOMLEFT, LinkArrow.BOTTOMRIGHT), holly.monster?.arrows)
    }

    @Test
    fun marincessCardsHaveCorrectStats() {
        val blueTang = Aoi.byName("Marincess Blue Tang")!!
        assertEquals(1500, blueTang.monster?.atk)
        assertEquals(1200, blueTang.monster?.def)
        assertEquals(4, blueTang.monster?.level)
        assertEquals(WATER, blueTang.monster?.attribute)
        assertEquals(CYBERSE, blueTang.monster?.race)

        val wonderHeart = Aoi.byName("Marincess Wonder Heart")!!
        assertEquals(2400, wonderHeart.monster?.atk)
        assertEquals(4, wonderHeart.monster?.link)
        assertEquals(listOf(LinkArrow.LEFT, LinkArrow.RIGHT, LinkArrow.BOTTOMLEFT, LinkArrow.BOTTOMRIGHT), wonderHeart.monster?.arrows)
    }

    @Test
    fun triggerPointsAreSetOnCards() {
        val lycoris = Aoi.byName("Trickstar Lycoris")!!
        assertEquals(TriggerPoint.ON_ADD_TO_HAND, lycoris.trigger)

        val candina = Aoi.byName("Trickstar Candina")!!
        assertEquals(TriggerPoint.ON_SUMMON, candina.trigger)

        val wave = Aoi.byName("Marincess Wave")!!
        assertEquals(TriggerPoint.ON_ATTACK_DECLARE, wave.trigger)
    }
}
