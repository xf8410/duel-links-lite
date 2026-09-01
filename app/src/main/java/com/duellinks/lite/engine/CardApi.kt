package com.duellinks.lite.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class ApiResponse(val data: List<ApiFullCard> = emptyList())

@Serializable
private data class ApiImage(val image_url: String = "")

@Serializable
data class ApiFullCard(
    val id: Long = 0,
    val name: String = "",
    val desc: String = "",
    val type: String = "",
    val frameType: String = "",
    val atk: Int? = null,
    val def: Int? = null,
    val level: Int? = null,
    val race: String = "",
    val attribute: String = "",
    val linkval: Int? = null,
    val linkmarkers: List<String> = emptyList(),
    val card_images: List<ApiImage> = emptyList()
) {
    val imageUrl: String get() = card_images.firstOrNull()?.image_url ?: ""
}

/**
 * 运行时从 YGOPRODeck 公共 API 拉取官方卡数据（数值/属性/种族/连接/箭头/卡文/卡图）。
 * 第 1 次联网成功后写入缓存，断网也能用官方素材。
 */
object CardApi {
    private val json = Json { ignoreUnknownKeys = true }
    private const val ENDPOINT = "https://db.ygoprodeck.com/api/v7/cardinfo.php"
    private const val CACHE_MS = 7L * 24 * 3600 * 1000

    // 拉取完整卡库（卡名 -> 完整数据）。失败或离线返回空 Map。
    suspend fun fetchAll(cacheDir: File): Map<String, ApiFullCard> = withContext(Dispatchers.IO) {
        val cache = File(cacheDir, "ygopro_cards.json")
        try {
            val raw = readCachedOrFetch(ENDPOINT, cache)
            json.decodeFromString<ApiResponse>(raw)
                .data.associateBy { it.name }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // 按种族/系列拉取（如 archetype=Trickstar）。失败或离线返回空列表。
    suspend fun fetchArchetype(archetype: String, cacheDir: File): List<ApiFullCard> = withContext(Dispatchers.IO) {
        val url = "$ENDPOINT?archetype=$archetype"
        val cache = File(cacheDir, "ygopro_${archetype}_cards.json")
        try {
            val raw = readCachedOrFetch(url, cache)
            json.decodeFromString<ApiResponse>(raw).data
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun readCachedOrFetch(url: String, cache: File): String {
        if (cache.exists() && System.currentTimeMillis() - cache.lastModified() < CACHE_MS) return cache.readText()
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20000
            readTimeout = 20000
        }
        val raw = conn.inputStream.bufferedReader().use { it.readText() }
        cache.parentFile?.mkdirs()
        cache.writeText(raw)
        return raw
    }

    // ========== API 数据 -> 游戏 Card ==========

    fun cardType(frameType: String): CardType = when (frameType) {
        "spell", "spell_normal", "spell_field", "spell_quickplay", "spell_equip", "spell_ritual" -> CardType.SPELL
        "trap", "trap_normal", "trap_continuous", "trap_counter" -> CardType.TRAP
        else -> CardType.MONSTER
    }

    fun summonKind(frameType: String): SummonKind = when (frameType) {
        "fusion" -> SummonKind.FUSION
        "synchro" -> SummonKind.SYNCHRO
        "xyz" -> SummonKind.XYZ
        "link" -> SummonKind.LINK
        "ritual" -> SummonKind.RITUAL
        "pendulum" -> SummonKind.PENDULUM
        else -> SummonKind.NORMAL
    }

    fun attribute(s: String): Attribute = when (s) {
        "LIGHT" -> Attribute.LIGHT
        "DARK" -> Attribute.DARK
        "EARTH" -> Attribute.EARTH
        "WATER" -> Attribute.WATER
        "FIRE" -> Attribute.FIRE
        "WIND" -> Attribute.WIND
        "DIVINE" -> Attribute.DIVINE
        else -> Attribute.LIGHT
    }

    fun race(s: String): Race = when (s) {
        "Dragon" -> Race.DRAGON
        "Cyberse" -> Race.CYBERSE
        "Spellcaster" -> Race.SPELLCASTER
        "Warrior" -> Race.WARRIOR
        "Beast" -> Race.BEAST
        "Beast-Warrior" -> Race.BEASTWARRIOR
        "Machine" -> Race.MACHINE
        "Fiend" -> Race.FIEND
        "Aqua" -> Race.AQUA
        "Plant" -> Race.PLANT
        "Insect" -> Race.INSECT
        "Thunder" -> Race.THUNDER
        "Rock" -> Race.ROCK
        "Psychic" -> Race.PSYCHIC
        "Winged Beast" -> Race.WINGEDBEAST
        "Zombie" -> Race.ZOMBIE
        "Fairy" -> Race.FAIRY
        "Reptile" -> Race.REPTILE
        "Sea Serpent" -> Race.SEASERPENT
        "Dinosaur" -> Race.DINOSAUR
        else -> Race.CYBERSE
    }

    fun arrow(s: String): LinkArrow = when (s) {
        "Top" -> LinkArrow.TOP
        "Bottom" -> LinkArrow.BOTTOM
        "Left" -> LinkArrow.LEFT
        "Right" -> LinkArrow.RIGHT
        "Top-Left" -> LinkArrow.TOPLEFT
        "Top-Right" -> LinkArrow.TOPRIGHT
        "Bottom-Left" -> LinkArrow.BOTTOMLEFT
        "Bottom-Right" -> LinkArrow.BOTTOMRIGHT
        else -> LinkArrow.TOP
    }

    fun spellType(frameType: String): SpellType? = when (frameType) {
        "spell_field" -> SpellType.FIELD
        "spell_quickplay" -> SpellType.QUICKPLAY
        "spell_equip" -> SpellType.EQUIP
        "spell_ritual" -> SpellType.RITUAL
        else -> SpellType.NORMAL
    }

    fun trapType(frameType: String): TrapType? = when (frameType) {
        "trap_counter" -> TrapType.COUNTER
        "trap_continuous" -> TrapType.CONTINUOUS
        else -> TrapType.NORMAL
    }

    // 用 API 数据 + 手工效果映射构造一张卡。
    fun toCard(api: ApiFullCard, fx: CardFx?): Card {
        val ct = cardType(api.frameType)
        return Card(
            id = api.id.toString(),
            name = api.name,
            type = ct,
            kind = summonKind(api.frameType),
            monster = if (ct == CardType.MONSTER) {
                MonsterStats(
                    level = api.level,
                    rank = if (api.frameType == "xyz") api.level else null,
                    link = api.linkval,
                    atk = api.atk ?: 0,
                    def = api.def,
                    attribute = attribute(api.attribute),
                    race = race(api.race),
                    arrows = api.linkmarkers.map { arrow(it) }
                )
            } else null,
            spellType = if (ct == CardType.SPELL) spellType(api.frameType) else null,
            trapType = if (ct == CardType.TRAP) trapType(api.frameType) else null,
            text = api.desc,
            effectTags = fx?.tags ?: emptyList(),
            trigger = fx?.trigger,
            materials = fx?.materials ?: emptyList(),
            imageUrl = api.imageUrl
        )
    }
}

// 由于 YGOPRODeck API 只提供卡文文本，不提供结构化"效果"，游戏内的简化效果标签/触发点仍需手工标注。
data class CardFx(
    val tags: List<EffectTag> = emptyList(),
    val trigger: TriggerPoint? = null,
    val materials: List<String> = emptyList()
)
