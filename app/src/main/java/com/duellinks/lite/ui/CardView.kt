package com.duellinks.lite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duellinks.lite.engine.*

@Composable
fun CardFace(
    card: Card,
    modifier: Modifier = Modifier,
    faceDown: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val bg = when {
        faceDown -> Color(0xFF1B2A4A)
        card.type == CardType.MONSTER -> Color(0xFF3A2E1E)
        card.type == CardType.SPELL -> Color(0xFF143B2E)
        else -> Color(0xFF3B1430)
    }
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color.Yellow else Color.Gray.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        if (faceDown) {
            Text("?", color = Color.White, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
        } else {
            card.imageUrl?.let {
                AsyncImage(
                    model = it, contentDescription = card.name,
                    modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop, alpha = 0.35f
                )
            }
            Column(Modifier.fillMaxSize()) {
                Text(card.name, color = Color.White, fontSize = 9.sp, maxLines = 2)
                Spacer(Modifier.weight(1f))
                if (card.type == CardType.MONSTER) {
                    val ms = card.monster
                    Text("ATK ${ms?.atk ?: 0}", color = Color.Yellow, fontSize = 9.sp)
                    Text(
                        if (card.kind == SummonKind.XYZ || card.kind == SummonKind.LINK) "DEF -"
                        else "DEF ${ms?.def ?: 0}",
                        color = Color.White, fontSize = 9.sp
                    )
                    if (ms?.pendulumScale != null) Text("P ${ms.pendulumScale}", color = Color.Cyan, fontSize = 9.sp)
                } else {
                    Text(if (card.type == CardType.SPELL) "魔法" else "陷阱", color = Color.White, fontSize = 9.sp)
                }
            }
        }
    }
}
