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

    // 运行时由 MainActivity 用 YGOPRODeck API 的完整卡数据覆盖（数值/卡文/卡图）。离线时为空，回落内置数据。
    private var apiById: Map<String, ApiFullCard> = emptyMap()

    fun setApi(cards: Map<String, ApiFullCard>) {
        apiById = cards
    }

    // YGOPRODeck API 只提供卡文文本、不提供结构化"效果"，所以游戏内的简化效果标签/触发点/连接素材仍需手工标注。
    private val fxById: Map<String, CardFx> = mapOf(
        // 淘气仙星
        "61283655" to CardFx(listOf(DRAW_2), ON_SUMMON),
        "35199656" to CardFx(listOf(BURN_500), ON_ADD_TO_HAND),
        "98700941" to CardFx(listOf(REVIVE_ONE), ON_DESTROY),
        "22219822" to CardFx(listOf(DESTROY_ONE_MONSTER), ON_SUMMON),
        "86825114" to CardFx(),
        "91505214" to CardFx(listOf(BURN_500), ON_ADD_TO_HAND),
        "59604521" to CardFx(listOf(REVIVE_ONE), ON_SUMMON),
        "98169343" to CardFx(listOf(BUFF_SELF_500), ON_SUMMON),
        "1410324" to CardFx(),
        "35371948" to CardFx(listOf(BURN_500), ON_ADD_TO_HAND),
        "62481203" to CardFx(listOf(REVIVE_ONE)),
        "88693151" to CardFx(listOf(DRAW_2)),
        "99890852" to CardFx(listOf(BUFF_SELF_500)),
        "22159429" to CardFx(listOf(REVIVE_ONE)),
        "21076084" to CardFx(listOf(POP_SPELL_TRAP)),
        "32448765" to CardFx(listOf(BURN_500), ON_SUMMON, listOf("Trickstar")),
        "94626871" to CardFx(listOf(BURN_500), ON_DESTROY, listOf("Trickstar")),
        "14365823" to CardFx(listOf(BURN_500), ON_SUMMON, listOf("Trickstar")),
        "51011872" to CardFx(listOf(HEAL_1000), ON_SUMMON, listOf("Trickstar")),
        "3792766" to CardFx(listOf(DRAW_2), ON_ATTACK_DECLARE, listOf("Trickstar")),
        "86750474" to CardFx(listOf(BURN_500), ON_DESTROY, listOf("Fairy")),
        "41302052" to CardFx(listOf(BURN_500), ON_SUMMON, listOf("Trickstar")),
        "77307161" to CardFx(listOf(DRAW_2), ON_SUMMON, listOf("Trickstar")),
        // 海晶少女
        "91953000" to CardFx(listOf(DESTROY_ONE_MONSTER), ON_SUMMON),
        "99885917" to CardFx(listOf(REVIVE_ONE), ON_SUMMON),
        "36492575" to CardFx(listOf(REVIVE_ONE), ON_SUMMON),
        "62886670" to CardFx(listOf(BUFF_SELF_500)),
        "28174796" to CardFx(listOf(REVIVE_ONE), ON_SUMMON),
        "54569495" to CardFx(listOf(HEAL_1000), ON_ATTACK_DECLARE),
        "33945211" to CardFx(listOf(POP_SPELL_TRAP), ON_DESTROY),
        "21057444" to CardFx(listOf(BURN_500), ON_SUMMON),
        "57541158" to CardFx(listOf(DESTROY_ONE_MONSTER), ON_SUMMON),
        "91027843" to CardFx(listOf(BUFF_SELF_500)),
        "57329501" to CardFx(listOf(REVIVE_ONE)),
        "52945066" to CardFx(listOf(DESTROY_ONE_MONSTER), ON_ATTACK_DECLARE),
        "84430165" to CardFx(listOf(BURN_500), ON_DESTROY),
        "27012990" to CardFx(listOf(BUFF_SELF_500)),
        "83723605" to CardFx(listOf(REVIVE_ONE)),
        "80627281" to CardFx(listOf(REVIVE_ONE), ON_DESTROY),
        "19712214" to CardFx(listOf(DESTROY_ONE_MONSTER), ON_ATTACK_DECLARE),
        "79130389" to CardFx(listOf(REVIVE_ONE), ON_SUMMON, listOf("WATER")),
        "30691817" to CardFx(listOf(DRAW_2), ON_SUMMON, listOf("Marincess")),
        "43735670" to CardFx(listOf(REVIVE_ONE), ON_SUMMON, listOf("Marincess")),
        "67712104" to CardFx(listOf(HEAL_1000), ON_ATTACK_DECLARE, listOf("WATER")),
        "5524387" to CardFx(listOf(REVIVE_ONE), ON_SUMMON, listOf("WATER")),
        "84546257" to CardFx(listOf(POP_SPELL_TRAP), ON_SUMMON, listOf("Marincess")),
        "94207108" to CardFx(listOf(BUFF_SELF_500), ON_DESTROY, listOf("WATER")),
        "20934852" to CardFx(listOf(DESTROY_ONE_MONSTER), ON_ATTACK_DECLARE, listOf("WATER")),
        "47910940" to CardFx(listOf(DRAW_2), ON_TURN_START, listOf("WATER"))
    )

    // 离线兜底：仅提供数值与占位（卡文运行时用 API 覆盖）。结构与 API 一致。
    private val trickstarIds = listOf(
        "61283655", "35199656", "98700941", "22219822", "86825114", "91505214",
        "59604521", "98169343", "1410324",
        "35371948", "62481203", "88693151", "99890852", "22159429", "21076084",
        "32448765", "94626871", "14365823", "51011872", "3792766", "86750474",
        "41302052", "77307161"
    )

    private val marincessIds = listOf(
        "91953000", "99885917", "36492575", "62886670", "28174796", "54569495",
        "33945211", "21057444", "57541158",
        "91027843", "57329501",
        "52945066", "84430165", "27012990", "83723605", "80627281", "19712214",
        "79130389", "30691817", "43735670", "67712104", "5524387", "84546257",
        "94207108", "20934852", "47910940"
    )

    // 以 API 数据为主构造卡；离线时用内置数值兜底。
    private fun card(id: String): Card? {
        val api = apiById[id] ?: return fallbackCard(id)
        return CardApi.toCard(api, fxById[id])
    }

    private fun fallbackCard(id: String): Card? = when (id) {
        // 淘气仙星主卡组
        "61283655" -> Card(id, "Trickstar Candina", MONSTER, monster = MonsterStats(4, null, null, 1800, 400, LIGHT, FAIRY), effectTags = listOf(DRAW_2), trigger = ON_SUMMON)
        "35199656" -> Card(id, "Trickstar Lycoris", MONSTER, monster = MonsterStats(3, null, null, 1600, 1200, LIGHT, FAIRY), effectTags = listOf(BURN_500), trigger = ON_ADD_TO_HAND)
        "98700941" -> Card(id, "Trickstar Lilybell", MONSTER, monster = MonsterStats(2, null, null, 800, 2000, LIGHT, FAIRY), effectTags = listOf(REVIVE_ONE), trigger = ON_DESTROY)
        "22219822" -> Card(id, "Trickstar Mandrake", MONSTER, monster = MonsterStats(2, null, null, 0, 1000, LIGHT, FAIRY), effectTags = listOf(DESTROY_ONE_MONSTER), trigger = ON_SUMMON)
        "86825114" -> Card(id, "Trickstar Nightshade", MONSTER, monster = MonsterStats(1, null, null, 100, 0, LIGHT, FAIRY))
        "91505214" -> Card(id, "Trickstar Narkissus", MONSTER, monster = MonsterStats(4, null, null, 1000, 1800, LIGHT, FAIRY), effectTags = listOf(BURN_500), trigger = ON_ADD_TO_HAND)
        "59604521" -> Card(id, "Trickstar Rhodode", MONSTER, monster = MonsterStats(4, null, null, 1400, 1900, LIGHT, FAIRY), effectTags = listOf(REVIVE_ONE), trigger = ON_SUMMON)
        "98169343" -> Card(id, "Trickstar Corobane", MONSTER, monster = MonsterStats(5, null, null, 2000, 1000, LIGHT, FAIRY), effectTags = listOf(BUFF_SELF_500), trigger = ON_SUMMON)
        "1410324" -> Card(id, "Trickstar Hoody", MONSTER, monster = MonsterStats(2, null, null, 600, 1800, LIGHT, FAIRY))
        // 魔法/陷阱
        "35371948" -> Card(id, "Trickstar Light Stage", SPELL, spellType = SpellType.FIELD, effectTags = listOf(BURN_500), trigger = ON_ADD_TO_HAND)
        "62481203" -> Card(id, "Trickstar Festival", SPELL, spellType = SpellType.NORMAL, effectTags = listOf(REVIVE_ONE))
        "88693151" -> Card(id, "Trickstar Fusion", SPELL, spellType = SpellType.NORMAL, effectTags = listOf(DRAW_2))
        "99890852" -> Card(id, "Trickstar Bouquet", SPELL, spellType = SpellType.QUICKPLAY, effectTags = listOf(BUFF_SELF_500))
        "22159429" -> Card(id, "Trickstar Magical Laurel", SPELL, spellType = SpellType.EQUIP, effectTags = listOf(REVIVE_ONE))
        "21076084" -> Card(id, "Trickstar Reincarnation", TRAP, trapType = TrapType.NORMAL, effectTags = listOf(POP_SPELL_TRAP))
        // 淘气仙星连接
        "32448765" -> Card(id, "Trickstar Holly Angel", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 2, 2000, null, LIGHT, FAIRY, arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT)), effectTags = listOf(BURN_500), trigger = ON_SUMMON, materials = listOf("Trickstar"))
        "94626871" -> Card(id, "Trickstar Black Catbat", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 2, 2000, null, LIGHT, FAIRY, arrows = listOf(LEFT, RIGHT)), effectTags = listOf(BURN_500), trigger = ON_DESTROY, materials = listOf("Trickstar"))
        "14365823" -> Card(id, "Trickstar Divaridis", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 2, 1800, null, LIGHT, FAIRY, arrows = listOf(BOTTOMLEFT, BOTTOM)), effectTags = listOf(BURN_500), trigger = ON_SUMMON, materials = listOf("Trickstar"))
        "51011872" -> Card(id, "Trickstar Crimson Heart", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 2, 2000, null, LIGHT, FAIRY, arrows = listOf(RIGHT, BOTTOMLEFT)), effectTags = listOf(HEAL_1000), trigger = ON_SUMMON, materials = listOf("Trickstar"))
        "3792766" -> Card(id, "Trickstar Delfiendium", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 3, 2200, null, LIGHT, FAIRY, arrows = listOf(TOP, BOTTOMLEFT, BOTTOMRIGHT)), effectTags = listOf(DRAW_2), trigger = ON_ATTACK_DECLARE, materials = listOf("Trickstar"))
        "86750474" -> Card(id, "Trickstar Foxglove Witch", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 3, 2200, null, LIGHT, FAIRY, arrows = listOf(TOP, LEFT, RIGHT)), effectTags = listOf(BURN_500), trigger = ON_DESTROY, materials = listOf("Fairy"))
        "41302052" -> Card(id, "Trickstar Bella Madonna", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 4, 2800, null, LIGHT, FAIRY, arrows = listOf(TOP, RIGHT, BOTTOMLEFT, BOTTOM)), effectTags = listOf(BURN_500), trigger = ON_SUMMON, materials = listOf("Trickstar"))
        "77307161" -> Card(id, "Trickstar Bloom", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 1, 100, null, LIGHT, FAIRY, arrows = listOf(BOTTOM)), effectTags = listOf(DRAW_2), trigger = ON_SUMMON, materials = listOf("Trickstar"))
        // 海晶少女主卡组
        "91953000" -> Card(id, "Marincess Blue Tang", MONSTER, monster = MonsterStats(4, null, null, 1500, 1200, WATER, CYBERSE), effectTags = listOf(DESTROY_ONE_MONSTER), trigger = ON_SUMMON)
        "99885917" -> Card(id, "Marincess Pascalus", MONSTER, monster = MonsterStats(4, null, null, 1200, 2000, WATER, CYBERSE), effectTags = listOf(REVIVE_ONE), trigger = ON_SUMMON)
        "36492575" -> Card(id, "Marincess Sea Horse", MONSTER, monster = MonsterStats(3, null, null, 1400, 1000, WATER, CYBERSE), effectTags = listOf(REVIVE_ONE), trigger = ON_SUMMON)
        "62886670" -> Card(id, "Marincess Sea Star", MONSTER, monster = MonsterStats(2, null, null, 800, 400, WATER, CYBERSE), effectTags = listOf(BUFF_SELF_500))
        "28174796" -> Card(id, "Marincess Mandarin", MONSTER, monster = MonsterStats(1, null, null, 100, 100, WATER, CYBERSE), effectTags = listOf(REVIVE_ONE), trigger = ON_SUMMON)
        "54569495" -> Card(id, "Marincess Crown Tail", MONSTER, monster = MonsterStats(5, null, null, 600, 2300, WATER, CYBERSE), effectTags = listOf(HEAL_1000), trigger = ON_ATTACK_DECLARE)
        "33945211" -> Card(id, "Marincess Basilalima", MONSTER, monster = MonsterStats(4, null, null, 600, 2100, WATER, CYBERSE), effectTags = listOf(POP_SPELL_TRAP), trigger = ON_DESTROY)
        "21057444" -> Card(id, "Marincess Springirl", MONSTER, monster = MonsterStats(4, null, null, 1200, 1000, WATER, CYBERSE), effectTags = listOf(BURN_500), trigger = ON_SUMMON)
        "57541158" -> Card(id, "Marincess Sleepy Maiden", MONSTER, monster = MonsterStats(5, null, null, 500, 2500, WATER, CYBERSE), effectTags = listOf(DESTROY_ONE_MONSTER), trigger = ON_SUMMON)
        // 魔法/陷阱
        "91027843" -> Card(id, "Marincess Battle Ocean", SPELL, spellType = SpellType.FIELD, effectTags = listOf(BUFF_SELF_500))
        "57329501" -> Card(id, "Marincess Dive", SPELL, spellType = SpellType.NORMAL, effectTags = listOf(REVIVE_ONE))
        "52945066" -> Card(id, "Marincess Wave", TRAP, trapType = TrapType.NORMAL, effectTags = listOf(DESTROY_ONE_MONSTER), trigger = ON_ATTACK_DECLARE)
        "84430165" -> Card(id, "Marincess Current", TRAP, trapType = TrapType.NORMAL, effectTags = listOf(BURN_500), trigger = ON_DESTROY)
        "27012990" -> Card(id, "Marincess Cascade", TRAP, trapType = TrapType.NORMAL, effectTags = listOf(BUFF_SELF_500))
        "83723605" -> Card(id, "Marincess Circulation", TRAP, trapType = TrapType.NORMAL, effectTags = listOf(REVIVE_ONE))
        "80627281" -> Card(id, "Marincess Snow", TRAP, trapType = TrapType.NORMAL, effectTags = listOf(REVIVE_ONE), trigger = ON_DESTROY)
        "19712214" -> Card(id, "Marincess Bubble Ring", TRAP, trapType = TrapType.NORMAL, effectTags = listOf(DESTROY_ONE_MONSTER), trigger = ON_ATTACK_DECLARE)
        // 海晶少女连接
        "79130389" -> Card(id, "Marincess Coral Anemone", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 2, 2000, null, WATER, CYBERSE, arrows = listOf(LEFT, BOTTOM)), effectTags = listOf(REVIVE_ONE), trigger = ON_SUMMON, materials = listOf("WATER"))
        "30691817" -> Card(id, "Marincess Sea Angel", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 1, 1000, null, WATER, CYBERSE, arrows = listOf(LEFT)), effectTags = listOf(DRAW_2), trigger = ON_SUMMON, materials = listOf("Marincess"))
        "43735670" -> Card(id, "Marincess Blue Slug", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 1, 1500, null, WATER, CYBERSE, arrows = listOf(BOTTOM)), effectTags = listOf(REVIVE_ONE), trigger = ON_SUMMON, materials = listOf("Marincess"))
        "67712104" -> Card(id, "Marincess Crystal Heart", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 2, 0, null, WATER, CYBERSE, arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT)), effectTags = listOf(HEAL_1000), trigger = ON_ATTACK_DECLARE, materials = listOf("WATER"))
        "5524387" -> Card(id, "Marincess Marbled Rock", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 3, 2500, null, WATER, CYBERSE, arrows = listOf(LEFT, RIGHT, BOTTOM)), effectTags = listOf(REVIVE_ONE), trigger = ON_SUMMON, materials = listOf("WATER"))
        "84546257" -> Card(id, "Marincess Coral Triangle", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 3, 1500, null, WATER, CYBERSE, arrows = listOf(TOP, BOTTOMLEFT, BOTTOMRIGHT)), effectTags = listOf(POP_SPELL_TRAP), trigger = ON_SUMMON, materials = listOf("Marincess"))
        "94207108" -> Card(id, "Marincess Wonder Heart", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 4, 2400, null, WATER, CYBERSE, arrows = listOf(LEFT, RIGHT, BOTTOMLEFT, BOTTOMRIGHT)), effectTags = listOf(BUFF_SELF_500), trigger = ON_DESTROY, materials = listOf("WATER"))
        "20934852" -> Card(id, "Marincess Aqua Argonaut", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 4, 2300, null, WATER, CYBERSE, arrows = listOf(TOP, LEFT, RIGHT, BOTTOM)), effectTags = listOf(DESTROY_ONE_MONSTER), trigger = ON_ATTACK_DECLARE, materials = listOf("WATER"))
        "47910940" -> Card(id, "Marincess Great Bubble Reef", MONSTER, kind = SummonKind.LINK, monster = MonsterStats(null, null, 4, 2600, null, WATER, CYBERSE, arrows = listOf(LEFT, RIGHT, BOTTOM, BOTTOMRIGHT)), effectTags = listOf(DRAW_2), trigger = ON_TURN_START, materials = listOf("WATER"))
        else -> null
    }

    fun allCards(): List<Card> = (trickstarIds + marincessIds).mapNotNull { card(it) }
    fun byName(name: String): Card? = allCards().firstOrNull { it.name == name }

    private fun fill(main: List<Card>): List<Card> =
        main + List((20 - main.size).coerceAtLeast(0)) { main.first() }

    fun deckFor(skin: Skin): Pair<List<Card>, List<Card>> {
        val ids = if (skin.deckTag == "marincess") marincessIds else trickstarIds
        val all = ids.mapNotNull { card(it) }
        val main = all.filter { it.type == MONSTER && it.kind == SummonKind.NORMAL }
        val extra = all.filter { it.kind == SummonKind.LINK || it.kind == SummonKind.FUSION || it.kind == SummonKind.SYNCHRO || it.kind == SummonKind.XYZ }
        return fill(main) to extra
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
