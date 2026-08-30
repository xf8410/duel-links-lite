package com.duellinks.lite.engine

import com.duellinks.lite.engine.Attribute.*
import com.duellinks.lite.engine.CardType.*
import com.duellinks.lite.engine.EffectTag.*
import com.duellinks.lite.engine.LinkArrow.*
import com.duellinks.lite.engine.Race.*
import com.duellinks.lite.engine.SpellType.*
import com.duellinks.lite.engine.SummonKind.*
import com.duellinks.lite.engine.TriggerPoint.*
import com.duellinks.lite.engine.TrapType.*

enum class Skin(val label: String, val deckTag: String) {
    BLUE_ANGEL("蓝色天使", "trickstar"),
    BLUE_GIRL("蓝色女孩", "trickstar_plus"),
    BLUE_MAIDEN("蓝色少女和水灵儿", "marincess")
}

data class SkillMeta(
    val id: String,
    val label: String,
    val desc: String,
    val setupExtra: List<String> = emptyList(),
    val passiveFlags: Set<String> = emptySet()
)

object Aoi {

    val skills: List<SkillMeta> = listOf(
        SkillMeta(
            "my_all", "我现在的全力！",
            "①决斗开始时：向额外卡组加1只「淘气仙星·蜀葵天使」（至多4张）。" +
                "②第2回合后、自己通常抽卡前可用1次：此回合的1张普通抽卡改为从卡组随机选1只天使族/光属性怪兽。",
            setupExtra = listOf("Trickstar Holly Angel")
        ),
        SkillMeta(
            "ocean_blue", "海洋母亲的深蓝",
            "①我方只能召唤/特殊召唤水属性怪兽，「海晶少女」以外我方卡片效果向对手造成的效果伤害变为0。" +
                "②展示手牌1只等级4「海晶少女」怪兽或1张「海晶少女」魔法卡：从卡组将2只「海晶少女」怪兽加入手牌，然后选我方1张手牌送墓或回卡组。" +
                "③对手场上有怪兽、或我方场上有连接4怪兽时：从卡组将1张「海晶少女」魔法/陷阱加入手牌，然后可从额外特召1只「海晶少女 奇迹心」。",
            passiveFlags = setOf("WATER_ONLY_SUMMON")
        ),
        SkillMeta(
            "new_possibility", "水灵儿和我的全新可能性！",
            "①决斗开始时：向额外卡组各加1只「海晶少女 魔泡大堡垒」「海晶少女 珊瑚海葵」（每种至多4张）。" +
                "②我方场上有连接怪兽时可用1次：选墓地1只「海晶少女」怪兽以表侧守备特召。",
            setupExtra = listOf("Marincess Great Bubble Reef", "Marincess Coral Anemone")
        )
    )

    private fun mon(
        id: String, name: String, level: Int?, rank: Int?, link: Int?,
        atk: Int, def: Int?, attr: Attribute, race: Race, kind: SummonKind = SummonKind.NORMAL,
        tud: Boolean = false, pend: Int? = null, arrows: List<LinkArrow> = emptyList(),
        tags: List<EffectTag> = emptyList(), mats: List<String> = emptyList(), text: String = "",
        img: String? = null, trig: TriggerPoint? = null
    ) = Card(id, name, MONSTER, kind, MonsterStats(level, rank, link, atk, def, attr, race, tud, pend, arrows), null, null, text, tags, mats, img, trig)

    private fun spell(id: String, name: String, st: SpellType, tags: List<EffectTag> = emptyList(), text: String = "", img: String? = null, trig: TriggerPoint? = null) =
        Card(id, name, SPELL, spellType = st, effectTags = tags, text = text, imageUrl = img, trigger = trig)

    private fun trap(id: String, name: String, tt: TrapType, tags: List<EffectTag> = emptyList(), text: String = "", img: String? = null, trig: TriggerPoint? = null) =
        Card(id, name, TRAP, trapType = tt, effectTags = tags, text = text, imageUrl = img, trigger = trig)

    private fun img(id: String) = "https://images.ygoprodeck.com/images/cards/$id.jpg"

    // ========== 淘气仙星 (Trickstar) — 数据来自 YGOPRODeck API ==========
    private val trickstarCards: List<Card> = listOf(
        mon("61283655", "Trickstar Candina", 4, null, null, 1800, 400, LIGHT, FAIRY,
            tags = listOf(DRAW_2), trig = ON_SUMMON,
            text = "When this card is Normal Summoned: You can add 1 \"Trickstar\" card from your Deck to your hand. Each time your opponent activates a Spell/Trap Card, inflict 200 damage to them immediately after it resolves.",
            img = img("61283655")),
        mon("35199656", "Trickstar Lycoris", 3, null, null, 1600, 1200, LIGHT, FAIRY,
            tags = listOf(BURN_500), trig = ON_ADD_TO_HAND,
            text = "(Quick Effect): You can reveal this card in your hand, then target 1 \"Trickstar\" monster you control, except \"Trickstar Lycoris\"; Special Summon this card, and if you do, return that monster to the hand. Each time a card(s) is added to your opponent's hand, inflict 200 damage to them for each.",
            img = img("35199656")),
        mon("98700941", "Trickstar Lilybell", 2, null, null, 800, 2000, LIGHT, FAIRY,
            tags = listOf(REVIVE_ONE), trig = ON_DESTROY,
            text = "If this card is added to your hand, except by drawing it: You can Special Summon it from your hand. This card can attack directly. When this card inflicts battle damage to your opponent: You can target 1 \"Trickstar\" monster in your GY; add it to your hand.",
            img = img("98700941")),
        mon("22219822", "Trickstar Mandrake", 2, null, null, 0, 1000, LIGHT, FAIRY,
            tags = listOf(DESTROY_ONE_MONSTER), trig = ON_SUMMON,
            text = "If this card is sent from the hand to the GY: You can Special Summon this card in Defense Position, but banish it when it leaves the field. If this card is sent to the GY as Link Material for the Link Summon of a \"Trickstar\" monster: You can target 1 monster an opponent's Link Monster points to; destroy it.",
            img = img("22219822")),
        mon("86825114", "Trickstar Nightshade", 1, null, null, 100, 0, LIGHT, FAIRY,
            text = "If this card is sent to the GY as Link Material for the Link Summon of a \"Trickstar\" monster: You can Special Summon this card, but banish it when it leaves the field.",
            img = img("86825114")),
        mon("91505214", "Trickstar Narkissus", 4, null, null, 1000, 1800, LIGHT, FAIRY,
            tags = listOf(BURN_500), trig = ON_ADD_TO_HAND,
            text = "If your opponent takes effect damage (except during the Damage Step): You can Special Summon this card from your hand. Each time your opponent activates a monster effect in their hand or GY, inflict 200 damage to them immediately after it resolves.",
            img = img("91505214")),
        mon("59604521", "Trickstar Rhodode", 4, null, null, 1400, 1900, LIGHT, FAIRY,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "You can discard 1 \"Trickstar\" card, then target 1 \"Trickstar\" Link Monster in your GY; Special Summon it. Each time a card(s) is banished from your opponent's GY, immediately inflict 200 damage to your opponent for each card banished.",
            img = img("59604521")),
        mon("98169343", "Trickstar Corobane", 5, null, null, 2000, 1000, LIGHT, FAIRY,
            tags = listOf(BUFF_SELF_500), trig = ON_SUMMON,
            text = "If you control no monsters, or all monsters you control are \"Trickstar\" monsters: You can Special Summon this card from your hand. During the Damage Step, when your \"Trickstar\" monster battles an opponent's monster (Quick Effect): You can send this card from your hand to the GY; that monster you control gains ATK equal to its original ATK until the end of this turn.",
            img = img("98169343")),
        mon("1410324", "Trickstar Hoody", 2, null, null, 600, 1800, LIGHT, FAIRY,
            text = "If you control a \"Trickstar\" Fusion or Link Monster: You can Special Summon this card from your hand. If this card is sent to the GY as material for a \"Trickstar\" Link Monster: You can add 1 \"Trickstar Fusion\" or \"Trickstar Diffusion\" from your Deck to your hand.",
            img = img("1410324")),
        spell("35371948", "Trickstar Light Stage", SpellType.FIELD, tags = listOf(BURN_500), trig = ON_ADD_TO_HAND,
            text = "When this card is activated: You can add 1 \"Trickstar\" monster from your Deck to your hand. Once per turn: You can target 1 Set card in your opponent's Spell & Trap Zone; while this card is in the Field Zone, that Set card cannot be activated until the End Phase. Each time a \"Trickstar\" monster you control inflicts battle or effect damage to your opponent, inflict 200 damage to them.",
            img = img("35371948")),
        spell("62481203", "Trickstar Festival", SpellType.NORMAL, tags = listOf(REVIVE_ONE),
            text = "Special Summon 2 \"Trickstar Tokens\" (Fairy/LIGHT/Level 1/ATK 0/DEF 0). If a \"Trickstar\" monster(s) you control that was Special Summoned from the Extra Deck would be destroyed by battle or card effect, you can banish this card from your GY instead. You cannot Normal or Special Summon monsters the turn you activate this card, except \"Trickstar\" monsters.",
            img = img("62481203")),
        spell("88693151", "Trickstar Fusion", SpellType.NORMAL, tags = listOf(DRAW_2),
            text = "Fusion Summon 1 \"Trickstar\" Fusion Monster from your Extra Deck, using monsters from your hand or field as Fusion Material. You can banish this card from your GY, then target 1 \"Trickstar\" monster in your GY; add it to your hand.",
            img = img("88693151")),
        spell("99890852", "Trickstar Bouquet", SpellType.QUICKPLAY, tags = listOf(BUFF_SELF_500),
            text = "Target 1 \"Trickstar\" monster you control and 1 face-up monster on the field; return that \"Trickstar\" monster to the hand, and if you do, the other monster gains ATK equal to the original ATK of the returned monster, until the end of this turn.",
            img = img("99890852")),
        spell("22159429", "Trickstar Magical Laurel", SpellType.EQUIP, tags = listOf(REVIVE_ONE),
            text = "Activate this card by targeting 1 \"Trickstar\" monster in your GY; Special Summon it and equip it with this card. When this card leaves the field, destroy that monster. Once per turn, if the equipped monster inflicts battle or effect damage to your opponent: You can Special Summon 1 \"Trickstar\" monster from your hand.",
            img = img("22159429")),
        trap("21076084", "Trickstar Reincarnation", TrapType.NORMAL, tags = listOf(POP_SPELL_TRAP),
            text = "Banish your opponent's entire hand, and if you do, they draw the same number of cards. You can banish this card from your GY, then target 1 \"Trickstar\" monster in your GY; Special Summon it.",
            img = img("21076084")),
        mon("32448765", "Trickstar Holly Angel", null, null, 2, 2000, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "2 \"Trickstar\" monsters. Each time a \"Trickstar\" monster(s) is Normal or Special Summoned to a zone(s) this card points to, inflict 200 damage to your opponent. \"Trickstar\" monsters this card points to cannot be destroyed by battle or card effects.",
            mats = listOf("Trickstar"), img = img("32448765")),
        mon("94626871", "Trickstar Black Catbat", null, null, 2, 2000, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(LEFT, RIGHT), tags = listOf(BURN_500), trig = ON_DESTROY,
            text = "2 \"Trickstar\" monsters. Each time a monster(s) this card points to is destroyed by battle or card effect and sent to the GY, inflict 200 damage to your opponent.",
            mats = listOf("Trickstar"), img = img("94626871")),
        mon("14365823", "Trickstar Divaridis", null, null, 2, 1800, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOM), tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "2 Level 3 or lower \"Trickstar\" monsters. If this card is Special Summoned: You can inflict 200 damage to your opponent. If your opponent Normal or Special Summons a monster(s): Inflict 200 damage to your opponent.",
            mats = listOf("Trickstar"), img = img("14365823")),
        mon("51011872", "Trickstar Crimson Heart", null, null, 2, 2000, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(RIGHT, BOTTOMLEFT), tags = listOf(HEAL_1000), trig = ON_SUMMON,
            text = "2 \"Trickstar\" monsters. Each time a \"Trickstar\" monster(s) is Normal or Special Summoned to a zone(s) this card points to, gain 200 LP.",
            mats = listOf("Trickstar"), img = img("51011872")),
        mon("3792766", "Trickstar Delfiendium", null, null, 3, 2200, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(TOP, BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(DRAW_2), trig = ON_ATTACK_DECLARE,
            text = "2+ \"Trickstar\" monsters. When this card declares an attack while pointing to a \"Trickstar\" monster: You can target your banished \"Trickstar\" cards, up to the number of Link Monsters your opponent controls; add them to your hand, and if you do, this card gains 1000 ATK for each card added.",
            mats = listOf("Trickstar"), img = img("3792766")),
        mon("86750474", "Trickstar Foxglove Witch", null, null, 3, 2200, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(TOP, LEFT, RIGHT), tags = listOf(BURN_500), trig = ON_DESTROY,
            text = "2+ Fairy monsters. If this card is Special Summoned: You can inflict 200 damage to your opponent for each card they control. If this Link Summoned card is destroyed by battle or effect: You can Special Summon 1 Link-2 or lower \"Trickstar\" monster from your Extra Deck.",
            mats = listOf("Fairy"), img = img("86750474")),
        mon("41302052", "Trickstar Bella Madonna", null, null, 4, 2800, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(TOP, RIGHT, BOTTOMLEFT, BOTTOM), tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "2+ \"Trickstar\" monsters. While this Link Summoned card points to no monsters, it is unaffected by other cards' activated effects. If this card points to no monsters: You can inflict 200 damage to your opponent for each \"Trickstar\" monster in your GY with a different name.",
            mats = listOf("Trickstar"), img = img("41302052")),
        mon("77307161", "Trickstar Bloom", null, null, 1, 100, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(BOTTOM), tags = listOf(DRAW_2), trig = ON_SUMMON,
            text = "1 Level 2 or lower \"Trickstar\" monster. If this card is Link Summoned: You can make your opponent draw 1 card. If a face-up \"Trickstar\" monster this card points to is destroyed by battle or card effect: You can inflict 200 damage to your opponent for each card in their hand.",
            mats = listOf("Trickstar"), img = img("77307161"))
    )

    // ========== 海晶少女 (Marincess) — 数据来自 YGOPRODeck API ==========
    private val marincessCards: List<Card> = listOf(
        mon("91953000", "Marincess Blue Tang", 4, null, null, 1500, 1200, WATER, CYBERSE,
            tags = listOf(DESTROY_ONE_MONSTER), trig = ON_SUMMON,
            text = "If this card is Normal or Special Summoned: You can send 1 \"Marincess\" monster from your Deck to the GY, except \"Marincess Blue Tang\". If this card is sent to the GY as material for the Link Summon of a WATER monster: You can excavate the top 3 cards of your Deck, and if you do, you can add 1 excavated \"Marincess\" card to your hand.",
            img = img("91953000")),
        mon("99885917", "Marincess Pascalus", 4, null, null, 1200, 2000, WATER, CYBERSE,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "If this card is Normal or Special Summoned: You can Special Summon 1 \"Marincess\" monster from your hand in Defense Position, except \"Marincess Pascalus\". During your Main Phase, except the turn this card was sent to the GY: You can banish this card from your GY, then target 1 \"Marincess\" Spell/Trap in your GY; add it to your hand.",
            img = img("99885917")),
        mon("36492575", "Marincess Sea Horse", 3, null, null, 1400, 1000, WATER, CYBERSE,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "You can Special Summon this card (from your hand) to your zone a \"Marincess\" Link Monster points to. During your Main Phase, except the turn this card was sent to the GY: You can banish this card from your GY; Special Summon 1 WATER monster from your hand to your zone a \"Marincess\" Link Monster points to.",
            img = img("36492575")),
        mon("62886670", "Marincess Sea Star", 2, null, null, 800, 400, WATER, CYBERSE,
            tags = listOf(BUFF_SELF_500),
            text = "You can send this card from your hand to the GY, then target 1 \"Marincess\" monster you control; it gains 800 ATK until the end of this turn.",
            img = img("62886670")),
        mon("28174796", "Marincess Mandarin", 1, null, null, 100, 100, WATER, CYBERSE,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "If you control 2 or more \"Marincess\" monsters, while this card is in your hand or GY: You can target 1 WATER Link Monster you control; Special Summon this card to your zone that monster points to, but banish it when it leaves the field.",
            img = img("28174796")),
        mon("54569495", "Marincess Crown Tail", 5, null, null, 600, 2300, WATER, CYBERSE,
            tags = listOf(HEAL_1000), trig = ON_ATTACK_DECLARE,
            text = "During damage calculation, if a monster battles another monster (Quick Effect): You can send 1 other \"Marincess\" monster from your hand to the GY; Special Summon this card from your hand, and if you do, the damage you take from that battle is halved.",
            img = img("54569495")),
        mon("33945211", "Marincess Basilalima", 4, null, null, 600, 2100, WATER, CYBERSE,
            tags = listOf(POP_SPELL_TRAP), trig = ON_DESTROY,
            text = "If a monster(s) you control would be destroyed by card effect, you can banish this card from your GY instead. You can banish 1 \"Marincess\" Trap from your GY; add 1 \"Marincess\" Trap with a different name from your Deck to your hand. If this card is banished: You can target 1 face-up monster you control; it gains 600 ATK.",
            img = img("33945211")),
        mon("21057444", "Marincess Springirl", 4, null, null, 1200, 1000, WATER, CYBERSE,
            tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "You can banish 1 \"Marincess\" monster from your GY; Special Summon this card from your hand. If this card is sent to the GY as material for the Link Summon of a WATER monster: You can send cards from the top of your Deck to the GY, equal to the number of \"Marincess\" monsters you control, then inflict 200 damage to your opponent for each \"Marincess\" card sent.",
            img = img("21057444")),
        mon("57541158", "Marincess Sleepy Maiden", 5, null, null, 500, 2500, WATER, CYBERSE,
            tags = listOf(DESTROY_ONE_MONSTER), trig = ON_SUMMON,
            text = "You can target 1 \"Marincess\" card you control; Special Summon this card from your hand, and if you do, it gains this effect: While this card is in the Monster Zone, the targeted card cannot be destroyed by your opponent's card effects.",
            img = img("57541158")),
        spell("91027843", "Marincess Battle Ocean", SpellType.FIELD, tags = listOf(BUFF_SELF_500),
            text = "All \"Marincess\" monsters you control gain 200 ATK, also each one gains 600 ATK for each \"Marincess\" card equipped to it. When you Link Summon a \"Marincess\" monster to the Extra Monster Zone: You can equip up to 3 \"Marincess\" Link Monsters with different names from your GY to that Link Summoned monster.",
            img = img("91027843")),
        spell("57329501", "Marincess Dive", SpellType.NORMAL, tags = listOf(REVIVE_ONE),
            text = "Activate 1 of the following effects: Target 1 non-Link \"Marincess\" monster in your GY; Special Summon it. Or if \"Marincess Battle Ocean\" is in your Field Zone: Special Summon 1 \"Marincess\" monster from your Deck.",
            img = img("57329501")),
        trap("52945066", "Marincess Wave", TrapType.NORMAL, tags = listOf(DESTROY_ONE_MONSTER), trig = ON_ATTACK_DECLARE,
            text = "If you control a \"Marincess\" Link Monster: Target 1 face-up monster your opponent controls; negate that face-up monster's effects until the end of this turn, then, if you control a Link-2 or higher \"Marincess\" monster, all face-up monsters you currently control are unaffected by your opponent's card effects until the end of this turn.",
            img = img("52945066")),
        trap("84430165", "Marincess Current", TrapType.NORMAL, tags = listOf(BURN_500), trig = ON_DESTROY,
            text = "When a \"Marincess\" Link Monster you control destroys an opponent's monster by battle: Inflict damage to your opponent equal to the Link Rating of that monster you control x 400.",
            img = img("84430165")),
        trap("27012990", "Marincess Cascade", TrapType.NORMAL, tags = listOf(BUFF_SELF_500),
            text = "Banish any number of \"Marincess\" Link Monsters you control (until your next Standby Phase), then target 1 face-up monster on the field; it gains ATK equal to the total Link Rating of the monsters banished x 300, until the end of this turn.",
            img = img("27012990")),
        trap("83723605", "Marincess Circulation", TrapType.NORMAL, tags = listOf(REVIVE_ONE),
            text = "Target 1 WATER Link Monster you control; return it to the Extra Deck, and if you do, Special Summon 1 \"Marincess\" Link Monster from your Extra Deck with the same Link Rating but a different name. (This is treated as a Link Summon.)",
            img = img("83723605")),
        trap("80627281", "Marincess Snow", TrapType.NORMAL, tags = listOf(REVIVE_ONE), trig = ON_DESTROY,
            text = "If a \"Marincess\" Link Monster(s) you control is destroyed by battle or card effect: Target 1 of those monsters; Special Summon 1 \"Marincess\" Link Monster from your Extra Deck with a lower Link Rating than that monster. (This is treated as a Link Summon.)",
            img = img("80627281")),
        trap("19712214", "Marincess Bubble Ring", TrapType.NORMAL, tags = listOf(DESTROY_ONE_MONSTER), trig = ON_ATTACK_DECLARE,
            text = "When a monster declares an attack: Negate the attack, and if you do, Special Summon 1 \"Marincess Crystal Heart\" from your Extra Deck or GY.",
            img = img("19712214")),
        mon("79130389", "Marincess Coral Anemone", null, null, 2, 2000, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, BOTTOM), tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "2 WATER monsters. You can target 1 WATER monster with 1500 or less ATK in your GY; Special Summon it to your zone this card points to. If this card is sent from the field to the GY: You can target 1 \"Marincess\" card in your GY; add it to your hand.",
            mats = listOf("WATER"), img = img("79130389")),
        mon("30691817", "Marincess Sea Angel", null, null, 1, 1000, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT), tags = listOf(DRAW_2), trig = ON_SUMMON,
            text = "1 Level 4 or lower \"Marincess\" monster. If this card is Link Summoned: You can add 1 \"Marincess\" Spell from your Deck to your hand.",
            mats = listOf("Marincess"), img = img("30691817")),
        mon("43735670", "Marincess Blue Slug", null, null, 1, 1500, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(BOTTOM), tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "1 Level 4 or lower \"Marincess\" monster. If this card is Link Summoned: You can target 1 \"Marincess\" monster in your GY, except \"Marincess Blue Slug\"; add it to your hand.",
            mats = listOf("Marincess"), img = img("43735670")),
        mon("67712104", "Marincess Crystal Heart", null, null, 2, 0, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(HEAL_1000), trig = ON_ATTACK_DECLARE,
            text = "2 WATER monsters. Unaffected by your opponent's monster effects while this card is in the Extra Monster Zone. When this card, or your \"Marincess\" Link Monster this card points to, is targeted for an attack: You can send 1 \"Marincess\" monster from your hand to the GY; for that battle, your monster cannot be destroyed by battle and you take no battle damage.",
            mats = listOf("WATER"), img = img("67712104")),
        mon("5524387", "Marincess Marbled Rock", null, null, 3, 2500, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, RIGHT, BOTTOM), tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "2+ WATER monsters. You can target 1 \"Marincess\" card in your GY, except \"Marincess Marbled Rock\"; add it to your hand. When an opponent's monster declares an attack: You can send 1 \"Marincess\" monster from your hand to the GY; for that battle, monsters cannot be destroyed by battle, also you take no battle damage.",
            mats = listOf("WATER"), img = img("5524387")),
        mon("84546257", "Marincess Coral Triangle", null, null, 3, 1500, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(TOP, BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(POP_SPELL_TRAP), trig = ON_SUMMON,
            text = "2+ \"Marincess\" monsters. You can send 1 WATER monster from your hand to your GY; add 1 \"Marincess\" Trap from your Deck to your hand. If only your opponent controls a monster: You can banish this card from your GY; Special Summon WATER Link Monsters from your GY whose combined Link Ratings equal exactly 3.",
            mats = listOf("Marincess"), img = img("84546257")),
        mon("94207108", "Marincess Wonder Heart", null, null, 4, 2400, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, RIGHT, BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(BUFF_SELF_500), trig = ON_DESTROY,
            text = "2+ WATER monsters. Once per battle, during damage calculation, if this card battles a monster (Quick Effect): You can Special Summon 1 of your \"Marincess\" Monster Cards equipped to this card, also this card cannot be destroyed by that battle. If this card in its owner's possession is destroyed by an opponent's card: You can Special Summon 1 Link-3 or lower \"Marincess\" monster from your GY.",
            mats = listOf("WATER"), img = img("94207108")),
        mon("20934852", "Marincess Aqua Argonaut", null, null, 4, 2300, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(TOP, LEFT, RIGHT, BOTTOM), tags = listOf(DESTROY_ONE_MONSTER), trig = ON_ATTACK_DECLARE,
            text = "2+ WATER monsters. While this card is in the Extra Monster Zone, your opponent's monsters cannot attack any monsters, except this one. You can target 1 WATER monster you control and 1 card your opponent controls; return them to the hand.",
            mats = listOf("WATER"), img = img("20934852")),
        mon("47910940", "Marincess Great Bubble Reef", null, null, 4, 2600, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, RIGHT, BOTTOM, BOTTOMRIGHT), tags = listOf(DRAW_2), trig = ON_TURN_START,
            text = "2+ WATER monsters. Once per turn, during each Standby Phase: You can banish 1 WATER monster from your GY or face-up field; draw 1 card. Each time a monster(s) is banished face-up: This card gains 600 ATK for each, until the end of this turn. You can send 1 WATER monster from your hand to the GY; Special Summon 1 of your banished \"Marincess\" monsters.",
            mats = listOf("WATER"), img = img("47910940"))
    )

    fun allCards(): List<Card> = trickstarCards + marincessCards
    fun byName(name: String): Card? = allCards().firstOrNull { it.name == name }

    private fun fill(main: List<Card>): List<Card> =
        main + List((20 - main.size).coerceAtLeast(0)) { main.first() }

    fun deckFor(skin: Skin): Pair<List<Card>, List<Card>> = when (skin.deckTag) {
        "marincess" -> fill(marincessCards.filter { it.type == MONSTER && it.kind == SummonKind.NORMAL }) to marincessCards.filter { it.kind == SummonKind.LINK }
        else -> fill(trickstarCards.filter { it.type == MONSTER && it.kind == SummonKind.NORMAL }) to trickstarCards.filter { it.kind == SummonKind.LINK }
    }

    fun applySkillSetup(skill: SkillMeta, extra: List<Card>): List<Card> {
        var e = extra
        for (name in skill.setupExtra) {
            val c = byName(name) ?: continue
            if (e.none { it.name == name }) e = e + c
        }
        return e
    }
}
