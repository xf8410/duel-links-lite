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
            setupExtra = listOf("淘气仙星·蜀葵天使")
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
            setupExtra = listOf("海晶少女 魔泡大堡垒", "海晶少女 珊瑚海葵")
        )
    )

    private fun mon(
        id: String, name: String, level: Int?, rank: Int?, link: Int?,
        atk: Int, def: Int?, attr: Attribute, race: Race, kind: SummonKind = SummonKind.NORMAL,
        tud: Boolean = false, pend: Int? = null, arrows: List<LinkArrow> = emptyList(),
        tags: List<EffectTag> = emptyList(), mats: List<String> = emptyList(), text: String = "",
        img: String? = null, trig: TriggerPoint? = null
    ) = Card(id, name, MONSTER, kind, MonsterStats(level, rank, link, atk, def, attr, race, tud, pend, arrows), null, null, text, tags, mats, img, trig)

    private fun spell(id: String, name: String, st: SpellType, tags: List<EffectTag> = emptyList(), text: String = "", trig: TriggerPoint? = null) =
        Card(id, name, SPELL, spellType = st, effectTags = tags, text = text, trigger = trig)

    private fun trap(id: String, name: String, tt: TrapType, tags: List<EffectTag> = emptyList(), text: String = "", trig: TriggerPoint? = null) =
        Card(id, name, TRAP, trapType = tt, effectTags = tags, text = text, trigger = trig)

    // ========== 淘气仙星 ==========
    private val trickstarCards: List<Card> = listOf(
        mon("t_candina", "淘气仙星·坎蒂娜", 4, null, null, 1800, 400, LIGHT, FAIRY,
            tags = listOf(DRAW_2), trig = ON_SUMMON, text = "通常召唤成功时：从卡组检索1张「淘气仙星」卡。"),
        mon("t_lycoris", "淘气仙星·里可丽丝", 3, null, null, 1600, 1200, LIGHT, FAIRY,
            tags = listOf(BURN_500), trig = ON_ADD_TO_HAND, text = "对方将卡加入手牌时：给对方200伤害。"),
        mon("t_apple", "淘气仙星·林檎", 3, null, null, 1500, 800, LIGHT, FAIRY,
            tags = listOf(DESTROY_ONE_MONSTER), trig = ON_DESTROY, text = "此卡被破坏时：破坏对方1只怪兽。"),
        mon("t_cat", "淘气仙星·喵绒", 1, null, null, 700, 300, LIGHT, FAIRY,
            tags = listOf(BURN_500), trig = ON_ADD_TO_HAND, text = "对方将卡加入手牌时：给对方200伤害。"),
        mon("t_holly", "淘气仙星·蜀葵天使", null, null, 2, 2000, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT), tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "连接2：淘气仙星x1。连接召唤成功时：从墓地回收1只淘气仙星。", mats = listOf("淘气仙星怪兽1只")),
        spell("t_stage", "淘气仙星灯光舞台", SpellType.FIELD, tags = listOf(BURN_500), trig = ON_ADD_TO_HAND,
            text = "对方将卡加入手牌时：给对方200伤害。"),
        trap("t_festival", "淘气仙星·万圣节", TrapType.NORMAL, tags = listOf(BURN_500), trig = ON_SUMMON,
            text = "对方场上特殊召唤怪兽时：给予其攻击力数值的伤害。")
    )

    // ========== 海晶少女 ==========
    private val marincessCards: List<Card> = listOf(
        mon("m_blue", "海晶少女 蓝海", 4, null, null, 1600, 1000, WATER, CYBERSE,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON, text = "通常召唤成功时：从卡组特殊召唤1只「海晶少女」怪兽。"),
        mon("m_dolphin", "海晶少女 海豚", 4, null, null, 1200, 1400, WATER, CYBERSE,
            tags = listOf(REVIVE_ONE), trig = ON_SUMMON, text = "通常召唤成功时：从墓地回收1只「海晶少女」怪兽。"),
        mon("m_pascal", "海晶少女 帕斯卡", 3, null, null, 800, 1400, WATER, CYBERSE,
            tags = listOf(DESTROY_ONE_MONSTER), trig = ON_SUMMON, text = "通常召唤成功时：破坏对方1只表侧表示怪兽。"),
        mon("m_jelly", "海晶少女 水母", 3, null, null, 1400, 1000, WATER, CYBERSE,
            tags = listOf(BURN_500), trig = ON_SUMMON, text = "对方特殊召唤时：给对方500伤害。"),
        mon("m_tang", "海晶少女 珊瑚海葵", null, null, 2, 2000, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, BOTTOM), tags = listOf(REVIVE_ONE), trig = ON_SUMMON,
            text = "连接2：海晶少女x1。连接召唤成功时：从墓地特殊召唤1只「海晶少女」怪兽。", mats = listOf("海晶少女怪兽1只")),
        mon("m_reef", "海晶少女 魔泡大堡垒", null, null, 3, 2300, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, BOTTOM, RIGHT), tags = listOf(DRAW_2), trig = ON_SUMMON,
            text = "连接3：海晶少女x2。此卡连接的怪兽特殊召唤时：抽1张。", mats = listOf("海晶少女怪兽2只")),
        mon("m_heart", "海晶少女 奇迹心", null, null, 4, 2400, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(TOP, BOTTOM, LEFT, RIGHT), tags = listOf(BUFF_SELF_500), trig = ON_DESTROY,
            text = "连接4：海晶少女x3。每控制1只「海晶少女」：此卡攻击力+500。不会被对方卡的效果破坏。", mats = listOf("海晶少女怪兽3只")),
        trap("m_wave", "海晶少女波动", TrapType.NORMAL, tags = listOf(DESTROY_ONE_MONSTER), trig = ON_ATTACK_DECLARE,
            text = "对方怪兽发动攻击时：使其攻击无效并破坏。"),
        spell("m_bounce", "海晶少女大波浪", SpellType.NORMAL, tags = listOf(DRAW_2), text = "从卡组加2只「海晶少女」怪兽上手，然后1张手牌回卡组。")
    )

    fun allCards(): List<Card> = trickstarCards + marincessCards
    fun byName(name: String): Card? = allCards().firstOrNull { it.name == name }

    private fun fill(main: List<Card>): List<Card> =
        main + List((20 - main.size).coerceAtLeast(0)) { main.first() }

    fun deckFor(skin: Skin): Pair<List<Card>, List<Card>> = when (skin.deckTag) {
        "marincess" -> fill(marincessCards.filter { it.type == MONSTER }) to marincessCards.filter { it.kind == SummonKind.LINK }
        else -> fill(trickstarCards.filter { it.type == MONSTER }) to trickstarCards.filter { it.kind == SummonKind.LINK }
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
