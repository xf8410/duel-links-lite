package com.duellinks.lite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.duellinks.lite.engine.CardApi
import com.duellinks.lite.engine.CardDatabase
import com.duellinks.lite.ui.DuelApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启动即尝试从 YGOPRODeck 拉取官方卡文/卡图，失败则沿用内置数据。
        lifecycleScope.launch(Dispatchers.IO) {
            val data = CardApi.fetchAll(cacheDir)
            CardDatabase.enrich(data)
        }
        setContent { DuelApp() }
    }
}
