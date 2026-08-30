package com.duellinks.lite.engine

import com.duellinks.lite.engine.Attribute.*
import com.duellinks.lite.engine.CardType.*
import com.duellinks.lite.engine.EffectTag.*
import com.duellinks.lite.engine.LinkArrow.*
import com.duellinks.lite.engine.Race.*
import com.duellinks.lite.engine.SpellType.*
import com.duellinks.lite.engine.SummonKind.*
import com.duellinks.lite.engine.TrapType.*

// 财前葵的三个角色形态：虽是不同"角色"，但技能池互通。
enum class Skin(val label: String, val deckTag: String) {
    BLUE_ANGEL("蓝色天使", "trickstar"),
    BLUE_GIRL("蓝色女孩", "trickstar_plus"),
    BLUE_MAIDEN("蓝色少女和水灵儿", "marincess")
}

// 技能描述（共享技能池，任意形态可装）
data class SkillMeta(
    val id: String,
    val label: String,
    val desc: String,
    val setupExtra: List<String> = emptyList()   // 开局要加入额外卡组的卡名
)

object Aoi {

    // 技能互通：三个技能任意形态通用。
    val skills: List<SkillMeta> = listOf(
        SkillMeta("ocean_blue", "海洋母亲的深蓝",
            "①我方水属性怪兽不会被对方卡效果破坏（海晶少女除外）②/③ 检索并特召海晶少女。",
            setupExtra = listOf("海晶少女 奇迹心")),
        SkillMeta("my_all", "我现在的全力！",
            "①决斗开始时向额外卡组加入1只淘气仙星·蜀葵天使 ②第2回合后通常抽卡改为从卡组选光属性/天使加手。",
            setupExtra = listOf("淘气仙星·蜀葵天使")),
        SkillMeta("new_possibility", "水灵儿和我的全新可能性！",
            "①向额外卡组加入海晶少女 魔泡大堡垒、海晶少女 珊瑚海葵 ②有连接怪兽时从墓地守备特召海晶少女。",
            setupExtra = listOf("海晶少女 魔泡大堡垒", "海晶少女 珊瑚海葵"))
    )

    private fun mon(
        id: String, name: String, level: Int?, rank: Int?, link: Int?,
        atk: Int, def: Int?, attr: Attribute, race: Race, kind: SummonKind = SummonKind.NORMAL,
        tud: Boolean = false, pend: Int? = null, arrows: List<LinkArrow> = emptyList(),
        tags: List<EffectTag> = emptyList(), mats: List<String> = emptyList(), text: String = "", img: String? = null
    ) = Card(id, name, MONSTER, kind, MonsterStats(level, rank, link, atk, def, attr, race, tud, pend, arrows), null, null, text, tags, mats, img)

    private fun spell(id: String, name: String, st: SpellType, tags: List<EffectTag> = emptyList(), text: String = "") =
        Card(id, name, SPELL, spellType = st, effectTags = tags, text = text)

    private fun trap(id: String, name: String, tt: TrapType, text: String = "") =
        Card(id, name, TRAP, trapType = tt, text = text)

    // 淘气仙星 / 海晶少女 卡池
    private val trickstarCards: List<Card> = listOf(
        mon("t_candina", "淘气仙星·坎蒂娜", 4, null, null, 1800, 400, LIGHT, FAIRY, text = "登场时从卡组检索淘气仙星。"),
        mon("t_lycoris", "淘气仙星·里可丽丝", 3, null, null, 1600, 1200, LIGHT, FAIRY, tags = listOf(BURN_500), text = "对方将卡加入手牌时给其200点伤害。"),
        mon("t_apple", "淘气仙星·林檎", 3, null, null, 1500, 800, LIGHT, FAIRY, text = "淘气仙星怪兽。"),
        mon("t_cat", "淘气仙星·喵绒", 1, null, null, 700, 300, LIGHT, FAIRY, text = "淘气仙星怪兽。"),
        mon("t_holly", "淘气仙星·蜀葵天使", null, null, 2, 2000, null, LIGHT, FAIRY, kind = SummonKind.LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT), text = "连接2：淘气仙星x1。", mats = listOf("淘气仙星怪兽1只")),
        spell("t_stage", "淘气仙星灯光舞台", SpellType.FIELD, text = "对方将卡加入手牌时给其200点伤害。"),
        trap("t_festival", "淘气仙星·万圣节", TrapType.NORMAL, text = "对方场上特殊召唤怪兽时，给予其攻击力数值的伤害。")
    )

    private val marincessCards: List<Card> = listOf(
        mon("m_blue", "海晶少女 蓝海", 4, null, null, 1600, 1000, WATER, CYBERSE, text = "海晶少女怪兽。"),
        mon("m_dolphin", "海晶少女 海豚", 4, null, null, 1200, 1400, WATER, CYBERSE, text = "海晶少女怪兽。"),
        mon("m_wave", "海晶少女 波动", 4, null, null, 1400, 1500, WATER, CYBERSE, text = "海晶少女怪兽。"),
        mon("m_tang", "海晶少女 珊瑚海葵", null, null, 2, 2000, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, BOTTOM), text = "连接2：海晶少女x1。", mats = listOf("海晶少女怪兽1只")),
        mon("m_reef", "海晶少女 魔泡大堡垒", null, null, 3, 2300, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(LEFT, BOTTOM, RIGHT), text = "连接3：海晶少女x2。", mats = listOf("海晶少女怪兽2只")),
        mon("m_heart", "海晶少女 奇迹心", null, null, 4, 2400, null, WATER, CYBERSE, kind = SummonKind.LINK,
            arrows = listOf(TOP, BOTTOM, LEFT, RIGHT), text = "连接4：海晶少女x3。", mats = listOf("海晶少女怪兽3只")),
        spell("m_wavecyclone", "海晶少女波动", SpellType.NORMAL, tags = listOf(DRAW_2), text = "抽2张。")
    )

    fun allCards(): List<Card> = trickstarCards + marincessCards

    fun byName(name: String): Card? = allCards().firstOrNull { it.name == name }

    private fun deck(tag: String, main: List<Card>, extra: List<Card>): Pair<List<Card>, List<Card>> {
        // 不足20张时按需补满
        val filled = main + List((20 - main.size).coerceAtLeast(0)) { main.first() }
        return filled to extra
    }

    fun deckFor(skin: Skin): Pair<List<Card>, List<Card>> = when (skin.deckTag) {
        "trickstar_plus" -> deck("trickstar_plus", trickstarCards.filter { it.type == MONSTER }.take(10), trickstarCards.filter { it.kind == SummonKind.LINK })
        "marincess" -> deck("marincess", marincessCards.filter { it.type == MONSTER }.take(10), marincessCards.filter { it.kind == SummonKind.LINK })
        else -> deck("trickstar", trickstarCards.filter { it.type == MONSTER }.take(10), trickstarCards.filter { it.kind == SummonKind.LINK })
    }

    // 开局技能：把技能要求的卡加入我方额外卡组
    fun applySkillSetup(skill: SkillMeta, extra: List<Card>): List<Card> {
        var e = extra
        for (name in skill.setupExtra) {
            val c = byName(name) ?: continue
            if (e.none { it.name == name }) e = e + c
        }
        return e
    }
}
