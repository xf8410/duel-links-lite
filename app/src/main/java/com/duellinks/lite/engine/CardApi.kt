package com.duellinks.lite.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class ApiCard(
    val name: String = "",
    val desc: String = "",
    val card_images: List<ApiImage> = emptyList()
)

@Serializable
private data class ApiImage(val image_url: String = "")

/**
 * 运行时从 YGOPRODeck 公共 API 拉取官方卡文与卡图。
 * 第一次联网成功后写入缓存，之后断网也能用官方素材。
 */
object CardApi {
    private val json = Json { ignoreUnknownKeys = true }
    private const val ENDPOINT = "https://db.ygoprodeck.com/api/v7/cardinfo.php"
    private const val CACHE_MS = 7L * 24 * 3600 * 1000

    // 返回 卡名 -> (官方卡文, 官方卡图URL)。离线或失败时返回空 Map（回落到内置数据）。
    suspend fun fetchAll(cacheDir: File): Map<String, Pair<String, String>> = withContext(Dispatchers.IO) {
        val cache = File(cacheDir, "ygopro_cards.json")
        try {
            val raw = if (cache.exists() && System.currentTimeMillis() - cache.lastModified() < CACHE_MS) {
                cache.readText()
            } else {
                val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 20000
                    readTimeout = 20000
                }
                conn.inputStream.bufferedReader().use { it.readText() }.also { cache.writeText(it) }
            }
            json.decodeFromString<List<ApiCard>>(raw)
                .associate { it.name to (it.desc to (it.card_images.firstOrNull()?.image_url ?: "")) }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
