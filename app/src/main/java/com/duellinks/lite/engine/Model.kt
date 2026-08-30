package com.duellinks.lite.engine

import kotlinx.serialization.*

@Serializable
enum class CardType { MONSTER, SPELL, TRAP }

@Serializable
enum class SummonKind { NORMAL, FUSION, SYNCHRO, XYZ, LINK, RITUAL, PENDULUM }

@Serializable
enum class SpellType { NORMAL, CONTINUOUS, EQUIP, QUICKPLAY, FIELD, RITUAL }

@Serializable
enum class TrapType { NORMAL, CONTINUOUS, COUNTER }

@Serializable
enum class Attribute { LIGHT, DARK, EARTH, WATER, FIRE, WIND, DIVINE }

@Serializable
enum class Race {
    DRAGON, CYBERSE, SPELLCASTER, WARRIOR, BEAST, BEASTWARRIOR, MACHINE, FIEND,
    AQUA, PLANT, INSECT, THUNDER, ROCK, PSYCHIC, WINGEDBEAST, ZOMBIE,
    FAIRY, REPTILE, SEASERPENT, DINOSAUR
}

@Serializable
enum class LinkArrow { TOP, BOTTOM, LEFT, RIGHT, TOPLEFT, TOPRIGHT, BOTTOMLEFT, BOTTOMRIGHT }

@Serializable
enum class EffectTag {
    DRAW_2, HEAL_1000, DESTROY_ONE_MONSTER, DESTROY_ALL_MONSTERS,
    BURN_500, BUFF_SELF_500, POP_SPELL_TRAP, REVIVE_ONE
}

@Serializable
data class MonsterStats(
    val level: Int? = null,
    val rank: Int? = null,
    val link: Int? = null,
    val atk: Int,
    val def: Int? = null,
    val attribute: Attribute,
    val race: Race,
    val tuner: Boolean = false,
    val pendulumScale: Int? = null,
    val arrows: List<LinkArrow> = emptyList()
)

@Serializable
data class Card(
    val id: String,
    val name: String,
    val type: CardType,
    val kind: SummonKind = SummonKind.NORMAL,
    val monster: MonsterStats? = null,
    val spellType: SpellType? = null,
    val trapType: TrapType? = null,
    val text: String = "",
    val effectTags: List<EffectTag> = emptyList(),
    val materials: List<String> = emptyList(),
    val imageUrl: String? = null
)

@Serializable
enum class Position { ATTACK, DEFENSE_FACEUP, DEFENSE_FACEDOWN }

@Serializable
data class FieldMonster(
    val card: Card,
    val position: Position,
    val owner: Int,
    val canAttack: Boolean = true,
    val changedThisTurn: Boolean = false,
    val summonedThisTurn: Boolean = true,
    val overlay: List<Card> = emptyList(),
    val isPendulumFaceUp: Boolean = false
)

@Serializable
data class FieldSpellTrap(
    val card: Card,
    val faceUp: Boolean,
    val owner: Int
)

@Serializable
enum class Phase { DRAW, MAIN1, BATTLE, MAIN2, END }

@Serializable
data class PlayerState(
    val lp: Int = 4000,
    val deck: List<Card> = emptyList(),
    val hand: List<Card> = emptyList(),
    val extraDeck: List<Card> = emptyList(),
    val graveyard: List<Card> = emptyList(),
    val banished: List<Card> = emptyList(),
    val pendulumZone: List<Card> = emptyList()
)

@Serializable
data class GameState(
    val players: List<PlayerState> = List(2) { PlayerState() },
    val monsterZones: List<List<FieldMonster?>> = List(2) { List(3) { null } },
    val spellZones: List<List<FieldSpellTrap?>> = List(2) { List(3) { null } },
    val turn: Int = 0,
    val phase: Phase = Phase.DRAW,
    val turnCount: Int = 1,
    val winner: Int = -1,
    val normalSummonUsed: List<Boolean> = listOf(false, false),
    val log: List<String> = emptyList()
)

@Serializable
sealed interface Action {
    val player: Int
}

@Serializable
data class DrawAction(override val player: Int) : Action

@Serializable
data class SummonNormalAction(
    override val player: Int,
    val handIndex: Int,
    val zone: Int,
    val set: Boolean
) : Action

@Serializable
data class SummonTributeAction(
    override val player: Int,
    val handIndex: Int,
    val zone: Int,
    val tributes: List<Int>,
    val set: Boolean
) : Action

@Serializable
data class ActivateSpellAction(
    override val player: Int,
    val handIndex: Int,
    val zone: Int
) : Action

@Serializable
data class SetSpellAction(
    override val player: Int,
    val handIndex: Int,
    val zone: Int
) : Action

@Serializable
data class SetTrapAction(
    override val player: Int,
    val handIndex: Int,
    val zone: Int
) : Action

@Serializable
data class ActivateTrapAction(
    override val player: Int,
    val zone: Int
) : Action

@Serializable
data class ChangePositionAction(
    override val player: Int,
    val zone: Int
) : Action

@Serializable
data class AttackAction(
    override val player: Int,
    val attackerZone: Int,
    val targetPlayer: Int,
    val targetZone: Int
) : Action

@Serializable
data class SpecialSummonAction(
    override val player: Int,
    val kind: SummonKind,
    val zone: Int,
    val fromExtraIndex: Int = -1,
    val fromHandIndex: Int = -1,
    val materialHandIndices: List<Int> = emptyList(),
    val materialFieldZones: List<Int> = emptyList()
) : Action

@Serializable
data class NextPhaseAction(override val player: Int) : Action

@Serializable
data class SurrenderAction(override val player: Int) : Action
