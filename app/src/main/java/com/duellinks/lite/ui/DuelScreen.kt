package com.duellinks.lite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duellinks.lite.engine.*

@Composable
fun DuelApp() {
    val vm: DuelViewModel = viewModel()
    DuelTheme {
        if (vm.menuScreen.value) MenuScreen(vm) else DuelScreen(vm)
    }
}

@Composable
fun MenuScreen(vm: DuelViewModel) {
    var ip by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("决斗链接 Lite", style = MaterialTheme.typography.headlineMedium)
        Text("Kotlin 安卓版 · 融合/同调/超量/链接/灵摆", fontSize = 12.sp, color = Color.Gray)

        Button(onClick = { vm.start(Mode.AI) }, Modifier.fillMaxWidth()) { Text("单人 · 对战 AI") }
        Button(onClick = { vm.start(Mode.HOTSEAT) }, Modifier.fillMaxWidth()) { Text("双人 · 同屏热座") }
        Button(onClick = { vm.startNet(isHost = true) }, Modifier.fillMaxWidth()) { Text("联机 · 创建房间（主机）") }
        OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("对手设备的 IP") }, singleLine = true)
        Button(onClick = { vm.startNet(isHost = false, host = ip) }, Modifier.fillMaxWidth()) { Text("联机 · 加入房间") }
        if (vm.netError.value.isNotEmpty()) Text("网络: ${vm.netError.value}", color = Color.Red, fontSize = 12.sp)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Checkbox(checked = vm.useAoi.value, onCheckedChange = { vm.useAoi.value = it })
            Text("使用财前葵", fontSize = 13.sp)
        }
        if (vm.useAoi.value) {
            Text("形态（不同角色）", color = Color.Gray, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                Skin.entries.forEach { s ->
                    FilterChip(
                        selected = vm.selectedSkin.value == s,
                        onClick = { vm.selectedSkin.value = s },
                        label = { Text(s.label, fontSize = 9.sp) }
                    )
                }
            }
            // 技能三形态共享，解锁形态即可装备，不限制
            Text("技能（三形态共享）", color = Color.Gray, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                Aoi.skills.forEach { sk ->
                    FilterChip(
                        selected = vm.selectedSkill.value.id == sk.id,
                        onClick = { vm.selectedSkill.value = sk },
                        label = { Text(sk.label, fontSize = 9.sp) }
                    )
                }
            }
            Text(vm.selectedSkill.value.desc, color = Color.Cyan, fontSize = 10.sp)
        }
    }
}

@Composable
fun DuelScreen(vm: DuelViewModel) {
    val st = vm.state.value
    val op = vm.viewPlayer xor 1
    var showSpecial by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(4.dp)) {
        PlayerInfo(st, op, vm)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            for (z in 0..4) SpellZone(vm, op, z)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            for (z in 0..4) MonsterZone(vm, op, z)
        }
        Controls(vm) { showSpecial = true }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            for (z in 0..4) MonsterZone(vm, vm.viewPlayer, z)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            for (z in 0..4) SpellZone(vm, vm.viewPlayer, z)
        }
        PlayerInfo(st, vm.viewPlayer, vm)
        HandRow(vm)
        ActionBar(vm)
    }

    if (st.winner != -1) WinnerOverlay(vm)
    if (vm.passOverlay.value) PassOverlay(vm)
    if (vm.showLog.value) LogOverlay(vm, { vm.showLog.value = false })
    if (showSpecial) SpecialDialog(vm) { showSpecial = false }
}

@Composable
fun PlayerInfo(st: GameState, player: Int, vm: DuelViewModel) {
    val ps = st.players[player]
    val isOpp = player != vm.viewPlayer
    Row(
        Modifier.fillMaxWidth().padding(2.dp).background(Color(0xFF222A38)).padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${if (isOpp) "对手" else "你"}  LP ${ps.lp}", color = Color.White, fontSize = 14.sp)
        Text("卡组 ${ps.deck.size}  手牌 ${ps.hand.size}  额外 ${ps.extraDeck.size}", color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun MonsterZone(vm: DuelViewModel, owner: Int, zone: Int) {
    val st = vm.state.value
    val fm = st.monsterZones[owner][zone]
    val isOpp = owner != vm.viewPlayer
    Box(
        Modifier.size(60.dp, 84.dp).padding(2.dp)
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
    ) {
        if (fm == null) {
            if (vm.attackMode.value && isOpp && vm.isInteractive()) {
                Box(Modifier.fillMaxSize().clickable { vm.humanAttack(vm.selectedMonster.value, owner, -1) })
            }
        } else {
            val faceDown = fm.position == Position.DEFENSE_FACEDOWN && isOpp
            CardFace(
                fm.card, faceDown = faceDown,
                selected = !isOpp && vm.selectedMonster.value == zone,
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    if (!vm.isInteractive()) return@CardFace
                    if (vm.attackMode.value && isOpp) {
                        vm.humanAttack(vm.selectedMonster.value, owner, zone)
                    } else if (!isOpp) {
                        vm.selectedMonster.value = zone
                    }
                }
            )
        }
    }
}

@Composable
fun SpellZone(vm: DuelViewModel, owner: Int, zone: Int) {
    val st = vm.state.value
    val fs = st.spellZones[owner][zone]
    val isOpp = owner != vm.viewPlayer
    Box(
        Modifier.size(60.dp, 84.dp).padding(2.dp)
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
    ) {
        if (fs == null) {
            Box(Modifier.fillMaxSize())
        } else {
            val faceDown = !fs.faceUp && isOpp
            CardFace(
                fs.card, faceDown = faceDown, modifier = Modifier.fillMaxSize(),
                onClick = {
                    if (!vm.isInteractive()) return@CardFace
                    if (!isOpp && !fs.faceUp && fs.card.type == CardType.TRAP) {
                        vm.perform(ActivateTrapAction(vm.viewPlayer, zone))
                    }
                }
            )
        }
    }
}

@Composable
fun Controls(vm: DuelViewModel, onSpecial: () -> Unit) {
    val st = vm.state.value
    Row(
        Modifier.fillMaxWidth().padding(2.dp).background(Color(0xFF161B25)).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "回合${st.turnCount} · ${if (st.turn == vm.viewPlayer) "你的" else "对手"}回合 · ${st.phase}",
            color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f)
        )
        Button(onClick = onSpecial, enabled = vm.isInteractive()) { Text("特殊召唤", fontSize = 11.sp) }
        Button(onClick = { vm.showLog.value = true }) { Text("日志", fontSize = 11.sp) }
        Button(onClick = { vm.humanNextPhase() }, enabled = vm.isInteractive()) { Text("下一阶段", fontSize = 11.sp) }
        Button(onClick = { vm.humanSurrender() }, enabled = vm.isInteractive()) { Text("投降", fontSize = 11.sp) }
    }
}

@Composable
fun HandRow(vm: DuelViewModel) {
    val st = vm.state.value
    val hand = st.players[vm.viewPlayer].hand
    LazyRow(Modifier.fillMaxWidth().height(96.dp).padding(2.dp)) {
        items(hand.size) { i ->
            val card = hand[i]
            Box(Modifier.padding(2.dp).width(60.dp)) {
                CardFace(
                    card, selected = vm.selectedHand.value == i, modifier = Modifier.fillMaxSize(),
                    onClick = {
                        if (!vm.isInteractive()) return@CardFace
                        vm.selectedHand.value = if (vm.selectedHand.value == i) -1 else i
                        vm.selectedMonster.value = -1
                        vm.attackMode.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun ActionBar(vm: DuelViewModel) {
    val st = vm.state.value
    if (vm.selectedHand.value >= 0) {
        val card = st.players[vm.viewPlayer].hand.getOrNull(vm.selectedHand.value) ?: return
        Row(Modifier.fillMaxWidth().padding(2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (card.type == CardType.MONSTER) {
                Button(onClick = { vm.humanSummon(vm.selectedHand.value, false) }) { Text("召唤", fontSize = 11.sp) }
                Button(onClick = { vm.humanSummon(vm.selectedHand.value, true) }) { Text("盖放", fontSize = 11.sp) }
            } else if (card.type == CardType.SPELL) {
                Button(onClick = { vm.humanActivateSpell(vm.selectedHand.value) }) { Text("发动", fontSize = 11.sp) }
                Button(onClick = { vm.humanSet(vm.selectedHand.value) }) { Text("盖放", fontSize = 11.sp) }
            } else {
                Button(onClick = { vm.humanSet(vm.selectedHand.value) }) { Text("盖放", fontSize = 11.sp) }
            }
            Button(onClick = { vm.clearSelection() }) { Text("取消", fontSize = 11.sp) }
        }
    } else if (vm.selectedMonster.value >= 0 && !vm.attackMode.value) {
        val fm = st.monsterZones[vm.viewPlayer][vm.selectedMonster.value]
        Row(Modifier.fillMaxWidth().padding(2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { vm.humanChangePosition(vm.selectedMonster.value) }) { Text("变更表示", fontSize = 11.sp) }
            if (st.phase == Phase.BATTLE && fm?.canAttack == true)
                Button(onClick = { vm.attackMode.value = true }) { Text("攻击", fontSize = 11.sp) }
            Button(onClick = { vm.clearSelection() }) { Text("取消", fontSize = 11.sp) }
        }
    } else if (vm.attackMode.value) {
        Row(Modifier.fillMaxWidth().padding(2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "选择攻击目标：点击对手怪兽或场地",
                color = Color.Yellow, fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Button(onClick = { vm.clearSelection() }) { Text("取消", fontSize = 11.sp) }
        }
    }
}

@Composable
fun SpecialDialog(vm: DuelViewModel, onClose: () -> Unit) {
    var kind by remember { mutableStateOf(SummonKind.FUSION) }
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { Button(onClose) { Text("关闭") } },
        title = { Text("特殊召唤") },
        text = { SpecialPicker(vm, kind, onKind = { kind = it }) }
    )
}

@Composable
fun SpecialPicker(vm: DuelViewModel, kind: SummonKind, onKind: (SummonKind) -> Unit) {
    val st = vm.state.value
    val extra = st.players[vm.viewPlayer].extraDeck
    val hand = st.players[vm.viewPlayer].hand
    val items = when (kind) {
        SummonKind.PENDULUM -> hand.filter { it.monster?.pendulumScale != null }.map { it to true } +
            extra.filter { it.monster?.pendulumScale != null }.map { it to false }
        else -> extra.filter { it.kind == kind }.map { it to false }
    }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(SummonKind.FUSION, SummonKind.SYNCHRO, SummonKind.XYZ, SummonKind.LINK, SummonKind.PENDULUM)
                .forEach { k -> Button(onClick = { onKind(k) }, modifier = Modifier.weight(1f)) { Text(k.name, fontSize = 9.sp) } }
        }
        LazyColumn(Modifier.height(240.dp)) {
            items(items) { (card, isHand) ->
                val handIndex = if (isHand) hand.indexOf(card) else -1
                val extraIndex = if (isHand) -1 else extra.indexOf(card)
                Button(
                    onClick = {
                        val zone = vm.engine.emptyMonsterZone(vm.viewPlayer) ?: -1
                        if (zone >= 0) vm.autoSpecial(kind, extraIndex, handIndex, zone)
                    },
                    modifier = Modifier.fillMaxWidth().padding(2.dp)
                ) { Text(card.name, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun WinnerOverlay(vm: DuelViewModel) {
    val winner = vm.state.value.winner
    AlertDialog(
        onDismissRequest = { },
        confirmButton = { Button(onClick = { vm.menuScreen.value = true }) { Text("返回菜单") } },
        title = { Text("决斗结束") },
        text = { Text("${if (winner == vm.viewPlayer) "你" else "对手"} 获胜！") }
    )
}

@Composable
fun PassOverlay(vm: DuelViewModel) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = { Button(onClick = { vm.dismissPass() }) { Text("交给玩家${vm.passTo.value + 1}") } },
        title = { Text("请传递设备") },
        text = { Text("现在轮到玩家${vm.passTo.value + 1}，点击确认后查看你的手牌。") }
    )
}

@Composable
fun LogOverlay(vm: DuelViewModel, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { Button(onClose) { Text("关闭") } },
        title = { Text("对战日志") },
        text = {
            LazyColumn(Modifier.height(300.dp)) {
                items(vm.state.value.log) { line -> Text(line, fontSize = 11.sp, color = Color.White) }
            }
        }
    )
}
