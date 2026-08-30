package com.duellinks.lite.engine

import com.duellinks.lite.engine.Attribute.*
import com.duellinks.lite.engine.CardType.*
import com.duellinks.lite.engine.EffectTag.*
import com.duellinks.lite.engine.LinkArrow.*
import com.duellinks.lite.engine.Race.*
import com.duellinks.lite.engine.SpellType.*
import com.duellinks.lite.engine.SummonKind.*
import com.duellinks.lite.engine.TrapType.*

object CardDatabase {

    private const val IMG = "https://images.ygoprodeck.com/images/cards/"

    private fun mon(
        id: String, name: String, level: Int?, rank: Int?, link: Int?,
        atk: Int, def: Int?, attr: Attribute, race: Race, kind: SummonKind = NORMAL,
        tuner: Boolean = false, pendScale: Int? = null,
        arrows: List<LinkArrow> = emptyList(), tags: List<EffectTag> = emptyList(),
        materials: List<String> = emptyList(), text: String = "", image: String? = null
    ) = Card(
        id, name, MONSTER, kind,
        MonsterStats(level, rank, link, atk, def, attr, race, tuner, pendScale, arrows),
        null, null, text, tags, materials, image
    )

    private fun spell(id: String, name: String, stype: SpellType, tags: List<EffectTag> = emptyList(), text: String = "", image: String? = null) =
        Card(id, name, SPELL, spellType = stype, effectTags = tags, text = text, imageUrl = image)

    private fun trap(id: String, name: String, ttype: TrapType, text: String = "", image: String? = null) =
        Card(id, name, TRAP, trapType = ttype, text = text, imageUrl = image)

    val all: List<Card> = listOf(
        // ---- Normal monsters ----
        mon("89631139", "青眼白龙", 8, null, null, 3000, 2500, LIGHT, DRAGON, image = IMG + "89631139.jpg",
            text = "以高攻击力著称的传说之龙。"),
        mon("46986414", "黑魔术师", 7, null, null, 2500, 2100, DARK, SPELLCASTER, image = IMG + "46986414.jpg",
            text = "魔法师中最具攻击力者。"),
        mon("74677422", "真红眼黑龙", 7, null, null, 2400, 2000, DARK, DRAGON, image = IMG + "74677422.jpg",
            text = "在真红眼之中寄宿着真正的力量。"),
        mon("id_giant", "巨岩战士", 3, null, null, 1300, 1900, EARTH, WARRIOR, text = "拥有坚硬岩石身体的战士。"),
        mon("id_celtic", "凯尔特守护者", 4, null, null, 1400, 1200, EARTH, WARRIOR, text = "挥舞长剑的勇敢守护者。"),
        mon("id_mystical", "神秘之妖精", 4, null, null, 800, 2000, LIGHT, SPELLCASTER, text = "以高守备力著称的妖精。"),
        mon("id_alex", "亚历山大龙", 4, null, null, 2000, 100, LIGHT, DRAGON, text = "闪耀的宝石之龙。"),

        // ---- Effect monsters ----
        mon("id_kuriboh", "库里波", 1, null, null, 300, 200, LIGHT, FIEND, text = "看似弱小却有神秘力量。"),
        mon("id_petit", "小妖龙", 3, null, null, 1200, 900, WIND, DRAGON, text = "幼小的龙族怪兽。"),
        mon("id_magdark", "黑之魔法神官", 6, null, null, 2100, 2000, DARK, SPELLCASTER, text = "高阶魔法师。"),

        // ---- Tuners (同调素材) ----
        mon("63977008", "废品同调士", 5, null, null, 1300, 500, EARTH, DRAGON, tuner = true, image = IMG + "63977008.jpg",
            text = "调整：可与非调整怪兽同调召唤。"),
        mon("id_tune2", "调整幼龙", 2, null, null, 0, 0, WIND, DRAGON, tuner = true, text = "等级2调整。"),

        // ---- Synchro monsters ----
        mon("44508094", "星尘龙", 8, null, null, 2500, 2000, LIGHT, DRAGON, kind = SYNCHRO,
            materials = listOf("调整+1只以上非调整"), image = IMG + "44508094.jpg",
            text = "同调：调整+1只以上非调整。"),
        mon("id_junkwarrior", "废品战士", 5, null, null, 2300, 1500, DARK, WARRIOR, kind = SYNCHRO,
            materials = listOf("调整+1只以上非调整"), text = "同调：调整+1只以上非调整。"),

        // ---- Xyz monsters ----
        mon("84013237", "No.39 希望皇霍普", null, 4, null, 2500, 2000, LIGHT, WARRIOR, kind = XYZ,
            materials = listOf("等级4怪兽x2"), image = IMG + "84013237.jpg",
            text = "超量：等级4怪兽x2。"),
        mon("id_leviathan", "No.17 海恶龙", null, 4, null, 2000, 0, WATER, SEASERPENT, kind = XYZ,
            materials = listOf("等级4怪兽x2"), text = "超量：等级4怪兽x2。"),

        // ---- Link monsters ----
        mon("91968023", "连接蜘蛛", null, null, 2, 1000, null, EARTH, CYBERSE, kind = LINK,
            arrows = listOf(BOTTOMLEFT, BOTTOMRIGHT), image = IMG + "91968023.jpg",
            text = "连接2：通常怪兽1只。", materials = listOf("通常怪兽1只")),
        mon("id_decodetalker", "解码语者", null, null, 3, 2300, null, DARK, CYBERSE, kind = LINK,
            arrows = listOf(LEFT, BOTTOM, RIGHT), text = "连接3：效果怪兽2只以上。",
            materials = listOf("效果怪兽2只以上")),

        // ---- Pendulum monsters ----
        mon("16178681", "异色眼灵摆龙", 7, null, null, 2500, 2000, DARK, DRAGON, pendScale = 4,
            image = IMG + "16178681.jpg", text = "灵摆刻度4。可灵摆召唤。"),
        mon("id_pendmage", "灵摆魔术师", 5, null, null, 1500, 800, DARK, SPELLCASTER, pendScale = 8,
            text = "灵摆刻度8。"),
        mon("id_penddragon", "灵摆幼龙", 3, null, null, 1000, 700, LIGHT, DRAGON, pendScale = 1,
            text = "灵摆刻度1。"),

        // ---- Fusion monsters ----
        mon("id_blueeyesultimate", "青眼究极龙", 12, null, null, 4500, 3800, LIGHT, DRAGON, kind = FUSION,
            materials = listOf("青眼白龙", "青眼白龙", "青眼白龙"), text = "融合：青眼白龙x3。"),
        mon("id_darkmagiciangirl", "黑魔术少女", 6, null, null, 2000, 1700, DARK, SPELLCASTER, kind = FUSION,
            materials = listOf("黑魔术师", "库里波"), text = "融合：黑魔术师+库里波。"),

        // ---- Spells ----
        spell("24094653", "融合", NORMAL, text = "从手牌/场上将融合素材送墓，从额外卡组融合召唤。", image = IMG + "24094653.jpg"),
        spell("83764718", "死者苏生", NORMAL, tags = listOf(REVIVE_ONE),
            text = "复活任意一方墓地的1只怪兽。", image = IMG + "83764718.jpg"),
        spell("53129443", "黑洞", NORMAL, tags = listOf(DESTROY_ALL_MONSTERS),
            text = "破坏双方场上所有怪兽。", image = IMG + "53129443.jpg"),
        spell("12580477", "雷击", NORMAL, tags = listOf(DESTROY_ALL_MONSTERS),
            text = "破坏对方场上所有怪兽。", image = IMG + "12580477.jpg"),
        spell("55144522", "强欲之壶", NORMAL, tags = listOf(DRAW_2),
            text = "从卡组抽2张。", image = IMG + "55144522.jpg"),
        spell("05318639", "旋风", QUICKPLAY, tags = listOf(POP_SPELL_TRAP),
            text = "破坏场上1张魔法/陷阱卡。", image = IMG + "05318639.jpg"),
        spell("id_raigeki", "雷击·破", NORMAL, tags = listOf(BURN_500), text = "给对手500点伤害。"),

        // ---- Traps ----
        trap("44095762", "神圣防护罩-反射镜力-", NORMAL,
            text = "对手攻击宣言时，破坏对手所有攻击表示怪兽。", image = IMG + "44095762.jpg"),
        trap("04206964", "落穴", NORMAL,
            text = "对方召唤攻击力1000以上的怪兽时将其破坏。", image = IMG + "04206964.jpg"),
        trap("id_drain", "吸收护盾", NORMAL, text = "无效1次攻击并回复LP。")
    )

    private fun byId(id: String) = all.first { it.id == id }

    fun defaultDeck(): List<Card> = listOf(
        byId("89631139"), byId("89631139"), byId("74677422"), byId("46986414"),
        byId("id_giant"), byId("id_celtic"), byId("id_alex"), byId("id_mystical"),
        byId("id_petit"), byId("id_magdark"), byId("63977008"), byId("id_tune2"),
        byId("16178681"), byId("id_pendmage"), byId("id_penddragon"),
        byId("24094653"), byId("83764718"), byId("53129443"), byId("55144522"),
        byId("44095762")
    )

    fun defaultExtraDeck(): List<Card> = listOf(
        byId("id_blueeyesultimate"), byId("id_darkmagiciangirl"),
        byId("44508094"), byId("id_junkwarrior"),
        byId("84013237"), byId("id_leviathan"),
        byId("91968023"), byId("id_decodetalker")
    )
}
