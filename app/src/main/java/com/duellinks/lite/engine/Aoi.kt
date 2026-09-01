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

    // 运行时由 MainActivity 用 YGOPRODeck API 官方卡文/卡图覆盖；断网或失败时为空（回落内置数据）。
    private var apiPatch: Map<String, Pair<String, String>> = emptyMap()

    fun enrich(data: Map<String, Pair<String, String>>) {
        apiPatch = if (data.isEmpty()) emptyMap() else data
    }

    private fun p(c: Card): Card {
        val hit = apiPatch[c.name] ?: return c
        val (t, img) = hit
        return c.copy(
            text = if (t.isNotEmpty()) t else c.text,
            imageUrl = c.imageUrl ?: img.ifEmpty { null }
        )
    }

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
            text = "",
            img = img("61283655")),
        mon("35199656", "Trickstar Lycoris", 3, null, null, 1600, 1200, LIGHT, FAIRY,
            tags = listOf(BURN_500), trig = ON_ADD_TO_HAND,
            text = "",
            img = img("35199656")),
        mon("98700941", "Trickstar Lilybell", 2, null, null, 800, 2000, LIGHT, FAIRY,
            tags = listOf(REVIVE_ONE), trig = ON_DESTROY,
            text = "",
            img = img("98700941")),
        mon("22219822", "Trickstar Mandrake", 2, null, null, 0, 1000, LIGHT, FAIRY,
            tags = listOf(DESTROY_ONE_MONSTER), trig = ON_SUMMON,
            text = "",
            img = img("22219822")),
        mon("86825114", "Trickstar Nightshade", 1, null, null, 100, 0, LIGHT, FAIRY,
            text = "",
            img = img("86825114")),
        mon("91505214", "Trickstar Narkissus", 4, null, null, 1000, 1800, LIGHT, FAIRY,
            tags = listOf(BURN_500), trig = ON_ADD_TO_HAND,
            text = "",
            img = img("91505214")),
        mon("59604521", "Trickstar Rhodode", 4, null, null, 1400, 1900, LIGHT, FAIRY,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "",
            img = img("59604521")),
        mon("98169343", "Trickstar Corobane", 5, null, null, 2000, 1000, LIGHT, FAIRY,
            tags = listOf(BUFF_SELF_500), trig = ON_SUMMON,
            text = "",
            img = img("98169343")),
        mon("1410324", "Trickstar Hoody", 2, null, null, 600, 1800, LIGHT, FAIRY,
            text = "",
            img = img("1410324")),
        spell("35371948", "Trickstar Light Stage", SpellType.FIELD, tags = listOf(BURN_500), trig = ON_ADD_TO_HAND,
            text = "",
            img = img("35371948")),
        spell("62481203", "Trickstar Festival", SpellType.NORMAL, tags = listOf(REVIVE_ONE),
            text = "",
            img = img("62481203")),
        spell("88693151", "Trickstar Fusion", SpellType.NORMAL, tags = listOf(DRAW_2),
            text = "",
            img = img("88693151")),
        spell("99890852", "Trickstar Bouquet", SpellType.QUICKPLAY, tags = listOf(BUFF_SELF_500),
            text = "",
            img = img("99890852")),
        spell("22159429", "Trickstar Magical Laurel", SpellType.EQUIP, tags = listOf(REVIVE_ONE),
            text = "",
            img = img("22159429")),
        trap("21076084", "Trickstar Reincarnation", TrapType.NORMAL, tags = listOf(POP_SPELL_TRAP),
            text = "",
            img = img("21076084")),
        mon("32448765", "Trickstar Holly Angel", null, null, 2, 2000, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "",
            mats = listOf("Trickstar"), img = img("32448765")),
        mon("94626871", "Trickstar Black Catbat", null, null, 2, 2000, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(LEFT, RIGHT), tags = listOf(BURN_500), trig = ON_DESTROY,
            text = "",
            mats = listOf("Trickstar"), img = img("94626871")),
        mon("14365823", "Trickstar Divaridis", null, null, 2, 1800, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOM), tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "",
            mats = listOf("Trickstar"), img = img("14365823")),
        mon("51011872", "Trickstar Crimson Heart", null, null, 2, 2000, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(RIGHT, BOTTOMLEFT), tags = listOf(HEAL_1000), trig = ON_SUMMON,
            text = "",
            mats = listOf("Trickstar"), img = img("51011872")),
        mon("3792766", "Trickstar Delfiendium", null, null, 3, 2200, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(TOP, BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(DRAW_2), trig = ON_ATTACK_DECLARE,
            text = "",
            mats = listOf("Trickstar"), img = img("3792766")),
        mon("86750474", "Trickstar Foxglove Witch", null, null, 3, 2200, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(TOP, LEFT, RIGHT), tags = listOf(BURN_500), trig = ON_DESTROY,
            text = "",
            mats = listOf("Fairy"), img = img("86750474")),
        mon("41302052", "Trickstar Bella Madonna", null, null, 4, 2800, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(TOP, RIGHT, BOTTOMLEFT, BOTTOM), tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "",
            mats = listOf("Trickstar"), img = img("41302052")),
        mon("77307161", "Trickstar Bloom", null, null, 1, 100, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(BOTTOM), tags = listOf(DRAW_2), trig = ON_SUMMON,
            text = "",
            mats = listOf("Trickstar"), img = img("77307161"))
    )

    // ========== 海晶少女 (Marincess) — 数据来自 YGOPRODeck API ==========
    private val marincessCards: List<Card> = listOf(
        mon("91953000", "Marincess Blue Tang", 4, null, null, 1500, 1200, WATER, CYBERSE,
            tags = listOf(DESTROY_ONE_MONSTER), trig = ON_SUMMON,
            text = "",
            img = img("91953000")),
        mon("99885917", "Marincess Pascalus", 4, null, null, 1200, 2000, WATER, CYBERSE,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "",
            img = img("99885917")),
        mon("36492575", "Marincess Sea Horse", 3, null, null, 1400, 1000, WATER, CYBERSE,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "",
            img = img("36492575")),
        mon("62886670", "Marincess Sea Star", 2, null, null, 800, 400, WATER, CYBERSE,
            tags = listOf(BUFF_SELF_500),
            text = "",
            img = img("62886670")),
        mon("28174796", "Marincess Mandarin", 1, null, null, 100, 100, WATER, CYBERSE,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "",
            img = img("28174796")),
        mon("54569495", "Marincess Crown Tail", 5, null, null, 600, 2300, WATER, CYBERSE,
            tags = listOf(HEAL_1000), trig = ON_ATTACK_DECLARE,
            text = "",
            img = img("54569495")),
        mon("33945211", "Marincess Basilalima", 4, null, null, 600, 2100, WATER, CYBERSE,
            tags = listOf(POP_SPELL_TRAP), trig = ON_DESTROY,
            text = "",
            img = img("33945211")),
        mon("21057444", "Marincess Springirl", 4, null, null, 1200, 1000, WATER, CYBERSE,
            tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "",
            img = img("21057444")),
        mon("57541158", "Marincess Sleepy Maiden", 5, null, null, 500, 2500, WATER, CYBERSE,
            tags = listOf(DESTROY_ONE_MONSTER), trig = ON_SUMMON,
            text = "",
            img = img("57541158")),
        spell("91027843", "Marincess Battle Ocean", SpellType.FIELD, tags = listOf(BUFF_SELF_500),
            text = "",
            img = img("91027843")),
        spell("57329501", "Marincess Dive", SpellType.NORMAL, tags = listOf(REVIVE_ONE),
            text = "",
            img = img("57329501")),
        trap("52945066", "Marincess Wave", TrapType.NORMAL, tags = listOf(DESTROY_ONE_MONSTER), trig = ON_ATTACK_DECLARE,
            text = "",
            img = img("52945066")),
        trap("84430165", "Marincess Current", TrapType.NORMAL, tags = listOf(BURN_500), trig = ON_DESTROY,
            text = "",
            img = img("84430165")),
        trap("27012990", "Marincess Cascade", TrapType.NORMAL, tags = listOf(BUFF_SELF_500),
            text = "",
            img = img("27012990")),
        trap("83723605", "Marincess Circulation", TrapType.NORMAL, tags = listOf(REVIVE_ONE),
            text = "",
            img = img("83723605")),
        trap("80627281", "Marincess Snow", TrapType.NORMAL, tags = listOf(REVIVE_ONE), trig = ON_DESTROY,
            text = "",
            img = img("80627281")),
        trap("19712214", "Marincess Bubble Ring", TrapType.NORMAL, tags = listOf(DESTROY_ONE_MONSTER), trig = ON_ATTACK_DECLARE,
            text = "",
            img = img("19712214")),
        mon("79130389", "Marincess Coral Anemone", null, null, 2, 2000, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, BOTTOM), tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "",
            mats = listOf("WATER"), img = img("79130389")),
        mon("30691817", "Marincess Sea Angel", null, null, 1, 1000, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT), tags = listOf(DRAW_2), trig = ON_SUMMON,
            text = "",
            mats = listOf("Marincess"), img = img("30691817")),
        mon("43735670", "Marincess Blue Slug", null, null, 1, 1500, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(BOTTOM), tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "",
            mats = listOf("Marincess"), img = img("43735670")),
        mon("67712104", "Marincess Crystal Heart", null, null, 2, 0, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(HEAL_1000), trig = ON_ATTACK_DECLARE,
            text = "",
            mats = listOf("WATER"), img = img("67712104")),
        mon("5524387", "Marincess Marbled Rock", null, null, 3, 2500, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, RIGHT, BOTTOM), tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "",
            mats = listOf("WATER"), img = img("5524387")),
        mon("84546257", "Marincess Coral Triangle", null, null, 3, 1500, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(TOP, BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(POP_SPELL_TRAP), trig = ON_SUMMON,
            text = "",
            mats = listOf("Marincess"), img = img("84546257")),
        mon("94207108", "Marincess Wonder Heart", null, null, 4, 2400, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, RIGHT, BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(BUFF_SELF_500), trig = ON_DESTROY,
            text = "",
            mats = listOf("WATER"), img = img("94207108")),
        mon("20934852", "Marincess Aqua Argonaut", null, null, 4, 2300, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(TOP, LEFT, RIGHT, BOTTOM), tags = listOf(DESTROY_ONE_MONSTER), trig = ON_ATTACK_DECLARE,
            text = "",
            mats = listOf("WATER"), img = img("20934852")),
        mon("47910940", "Marincess Great Bubble Reef", null, null, 4, 2600, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, RIGHT, BOTTOM, BOTTOMRIGHT), tags = listOf(DRAW_2), trig = ON_TURN_START,
            text = "",
            mats = listOf("WATER"), img = img("47910940"))
    )

    fun allCards(): List<Card> = (trickstarCards + marincessCards).map(::p)
    fun byName(name: String): Card? = allCards().firstOrNull { it.name == name }

    private fun fill(main: List<Card>): List<Card> =
        main + List((20 - main.size).coerceAtLeast(0)) { main.first() }

    fun deckFor(skin: Skin): Pair<List<Card>, List<Card>> = when (skin.deckTag) {
        "marincess" -> fill(marincessCards.filter { it.type == MONSTER && it.kind == SummonKind.NORMAL }.map(::p)) to marincessCards.filter { it.kind == SummonKind.LINK }.map(::p)
        else -> fill(trickstarCards.filter { it.type == MONSTER && it.kind == SummonKind.NORMAL }.map(::p)) to trickstarCards.filter { it.kind == SummonKind.LINK }.map(::p)
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
