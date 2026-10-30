package com.example.randomizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val Night = Color(0xFF050B20)
private val Panel = Color(0xFF0A1738)
private val Blue = Color(0xFF42C7FF)
private val Ice = Color(0xFFB7E9FF)
private val Violet = Color(0xFF8A7CFF)
private val Muted = Color(0xFF9DB8DF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AndroidRasaApp() }
    }
}

@Composable
private fun AndroidRasaApp() {
    var page by remember { mutableStateOf("Главная") }
    var rolling by remember { mutableStateOf(false) }
    var result by remember { mutableIntStateOf(7) }
    var minValue by remember { mutableIntStateOf(1) }
    var maxValue by remember { mutableIntStateOf(12) }
    var history by remember { mutableStateOf(listOf<Int>()) }
    var sound by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val transition = rememberInfiniteTransition(label = "glow")
    val glow by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing)),
        label = "glowAlpha"
    )

    MaterialTheme(colorScheme = darkColorScheme(primary = Blue, secondary = Violet, background = Night, surface = Panel)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF071633), Night, Color(0xFF080D25))))) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Blue, Violet))), contentAlignment = Alignment.Center) {
                        Text("✦", color = Night, fontSize = 27.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("AndroidRasa", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ice)
                        Text("Случайность — тоже судьба", fontSize = 11.sp, color = Muted)
                    }
                    Text("✧", color = Blue, fontSize = 27.sp)
                }

                when (page) {
                    "Главная" -> HomeScreen(
                        result, rolling, glow, minValue, maxValue,
                        onRoll = {
                            if (!rolling) {
                                rolling = true
                                scope.launch {
                                    repeat(10) {
                                        result = Random.nextInt(minValue, maxValue + 1)
                                        delay(75)
                                    }
                                    result = Random.nextInt(minValue, maxValue + 1)
                                    history = (listOf(result) + history).take(10)
                                    rolling = false
                                }
                            }
                        },
                        onHistory = { page = "История бросков" }
                    )
                    "История бросков" -> HistoryScreen(history, { history = emptyList() })
                    "Настройки" -> SettingsScreen(
                        minValue, maxValue,
                        { minValue = it.coerceAtMost(maxValue - 1) },
                        { maxValue = it.coerceAtLeast(minValue + 1) },
                        sound, { sound = it }, vibration, { vibration = it }
                    )
                    "О приложении" -> AboutScreen()
                    else -> MenuScreen { page = it }
                }

                Spacer(Modifier.weight(1f, fill = false))
                NavigationBar(containerColor = Color(0xEE07112B), contentColor = Ice, tonalElevation = 0.dp) {
                    val pages = listOf("Главная", "История бросков", "Настройки", "Меню")
                    val labels = listOf("Бросок", "История", "Настройки", "Меню")
                    val icons = listOf("⚄", "☷", "⚙", "☰")
                    pages.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = page == destination,
                            onClick = { page = destination },
                            icon = { Text(icons[index], fontSize = 19.sp) },
                            label = { Text(labels[index], fontSize = 9.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Blue, selectedTextColor = Ice,
                                indicatorColor = Color(0xFF17366B), unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    result: Int, rolling: Boolean, glow: Float, minValue: Int, maxValue: Int,
    onRoll: () -> Unit, onHistory: () -> Unit
) {
    Column(Modifier.fillMaxWidth().fillMaxHeight(0.88f).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("TВОЯ ЛИЧНАЯ УДАЧА", color = Blue, fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF183A70), Color(0xFF101C48), Color(0xFF09122C))))
                .border(1.dp, Blue.copy(alpha = .4f), RoundedCornerShape(26.dp))
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                for (i in 0..44) {
                    drawCircle(Blue.copy(alpha = if (i % 3 == 0) .7f else .25f), radius = if (i % 4 == 0) 2.5f else 1.2f, center = Offset(i * 71f % w, i * 97f % h))
                }
                drawCircle(Blue.copy(alpha = .12f), radius = w * .37f, center = Offset(w * .5f, h * .38f))
                drawCircle(Blue.copy(alpha = .3f), radius = w * .31f, center = Offset(w * .5f, h * .38f), style = Stroke(2f))
            }
            Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Привет… я рада, что ты здесь", color = Ice, fontSize = 13.sp, textAlign = TextAlign.Center)
                Text("Проверим твою удачу?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HeroinePortrait(Modifier.fillMaxSize(.92f))
                    Column(Modifier.align(Alignment.BottomCenter), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(74.dp).background(Brush.linearGradient(listOf(Color(0xFFB8F4FF), Blue, Color(0xFF2A55DB))), RoundedCornerShape(20.dp))
                                .border(2.dp, Ice.copy(alpha = glow), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text(if (rolling) "✦" else "⚄", fontSize = 46.sp, color = Color.White, fontWeight = FontWeight.Black) }
                        Spacer(Modifier.height(5.dp))
                        Text(if (rolling) "Бросаю…" else "Выпало: $result", color = Ice, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text("Диапазон $minValue–$maxValue", color = Muted, fontSize = 11.sp)
                    }
                }
                Button(
                    onClick = onRoll, enabled = !rolling,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1267D8))
                ) { Text(if (rolling) "✧  МАГИЯ В ДЕЙСТВИИ…" else "⚄  БРОСИТЬ КУБИК", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
                Spacer(Modifier.height(7.dp))
                OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(15.dp)) {
                    Text("☷  История бросков", color = Ice)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("«Иногда самое простое — и есть самое волшебное…»", color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HeroinePortrait(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width * .5f
        val cy = size.height * .35f
        val hw = size.width * .27f
        val hh = size.height * .18f
        val hair = Brush.linearGradient(listOf(Color(0xFFEAFBFF), Color(0xFF8CCFFF), Color(0xFF526CB8)), Offset(cx - hw, 0f), Offset(cx + hw, size.height))
        val backHair = Path().apply {
            moveTo(cx - hw * .75f, cy - hh * .25f)
            quadraticTo(cx - hw * 1.4f, cy - hh * 1.4f, cx, cy - hh * 1.15f)
            quadraticTo(cx + hw * 1.45f, cy - hh * 1.2f, cx + hw * .8f, cy - hh * .1f)
            quadraticTo(cx + hw * 1.2f, cy + hh * 1.7f, cx + hw * .95f, cy + hh * 2.1f)
            quadraticTo(cx, cy + hh * 1.65f, cx - hw * .95f, cy + hh * 2.1f)
            quadraticTo(cx - hw * 1.25f, cy + hh * .8f, cx - hw * .75f, cy - hh * .25f)
            close()
        }
        drawPath(backHair, hair)
        val body = Path().apply {
            moveTo(cx - hw * .8f, cy + hh * .75f)
            quadraticTo(cx - hw * 1.8f, cy + hh * 1.05f, cx - hw * 2f, size.height)
            lineTo(cx + hw * 2f, size.height)
            quadraticTo(cx + hw * 1.8f, cy + hh * 1.05f, cx + hw * .8f, cy + hh * .75f)
            close()
        }
        drawPath(body, Brush.verticalGradient(listOf(Color(0xFFE7F8FF), Color(0xFF5E82D0), Color(0xFF182C66))))
        drawCircle(Color(0xFFFFE8EB), radius = hw * .7f, center = Offset(cx, cy + hh * .1f))
        val bangs = Path().apply {
            moveTo(cx - hw * .9f, cy)
            quadraticTo(cx - hw * .8f, cy - hh * 1.2f, cx, cy - hh * .95f)
            quadraticTo(cx + hw * .8f, cy - hh * 1.1f, cx + hw * .9f, cy)
            lineTo(cx + hw * .25f, cy - hh * .15f)
            lineTo(cx + hw * .05f, cy + hh * .28f)
            lineTo(cx - hw * .2f, cy - hh * .1f)
            lineTo(cx - hw * .55f, cy + hh * .25f)
            close()
        }
        drawPath(bangs, hair)
        drawOval(Color(0xFF1767B9), Offset(cx - hw * .48f, cy + hh * .05f), androidx.compose.ui.geometry.Size(hw * .34f, hh * .22f))
        drawOval(Color(0xFF1767B9), Offset(cx + hw * .14f, cy + hh * .05f), androidx.compose.ui.geometry.Size(hw * .34f, hh * .22f))
        drawCircle(Color.White, hw * .06f, Offset(cx - hw * .35f, cy + hh * .12f))
        drawCircle(Color.White, hw * .06f, Offset(cx + hw * .27f, cy + hh * .12f))
        drawCircle(Color(0xFFFF9BAF).copy(alpha = .55f), hw * .12f, Offset(cx - hw * .48f, cy + hh * .32f))
        drawCircle(Color(0xFFFF9BAF).copy(alpha = .55f), hw * .12f, Offset(cx + hw * .48f, cy + hh * .32f))
        val smile = Path().apply { moveTo(cx - hw * .12f, cy + hh * .34f); quadraticTo(cx, cy + hh * .45f, cx + hw * .12f, cy + hh * .34f) }
        drawPath(smile, Color(0xFFB85E7C), style = Stroke(2f))
        drawLine(Color.White.copy(alpha = .8f), Offset(cx - hw * .45f, cy - hh * .4f), Offset(cx - hw * .2f, cy - hh * .75f), 2f)
        drawLine(Ice, Offset(cx + hw * .35f, cy - hh * .35f), Offset(cx + hw * .55f, cy - hh * .65f), 2f)
    }
}

@Composable
private fun HistoryScreen(history: List<Int>, onClear: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("История бросков", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ice)
        Text("Последние 10 результатов", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        if (history.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚄", fontSize = 54.sp, color = Blue)
                    Text("Здесь появится твоя удача", color = Muted)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(history) { index, value ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel)
                            .border(1.dp, Blue.copy(alpha = .2f), RoundedCornerShape(14.dp)).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚄", fontSize = 23.sp, color = Blue)
                        Spacer(Modifier.width(14.dp))
                        Text("Бросок ${history.size - index}", color = Muted, modifier = Modifier.weight(1f))
                        Text(value.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ice)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("⌫  Очистить историю") }
        }
    }
}

@Composable
private fun SettingsScreen(
    minValue: Int, maxValue: Int, onMin: (Int) -> Unit, onMax: (Int) -> Unit,
    sound: Boolean, onSound: (Boolean) -> Unit, vibration: Boolean, onVibration: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Настройки", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ice)
        Text("Подстрой удачу под себя", color = Muted)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Диапазон чисел", color = Ice, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Text("От: $minValue", color = Muted)
                Slider(value = minValue.toFloat(), onValueChange = { onMin(it.toInt().coerceIn(1, maxValue - 1)) }, valueRange = 1f..(maxValue - 1).toFloat(), colors = SliderDefaults.colors(thumbColor = Blue, activeTrackColor = Blue))
                Text("До: $maxValue", color = Muted)
                Slider(value = maxValue.toFloat(), onValueChange = { onMax(it.toInt().coerceIn(minValue + 1, 20)) }, valueRange = (minValue + 1).toFloat()..20f, colors = SliderDefaults.colors(thumbColor = Blue, activeTrackColor = Blue))
                Text("Сейчас: $minValue–$maxValue", color = Blue, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Звук броска", color = Ice, modifier = Modifier.weight(1f))
                    Switch(checked = sound, onCheckedChange = onSound)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Вибрация", color = Ice, modifier = Modifier.weight(1f))
                    Switch(checked = vibration, onCheckedChange = onVibration)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("AndroidRasa • версия 2.0", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun MenuScreen(onNavigate: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Меню", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ice)
        Text("Всё нужное — рядом", color = Muted)
        Spacer(Modifier.height(18.dp))
        listOf("Главная" to "⚄", "История бросков" to "☷", "Настройки" to "⚙", "О приложении" to "✧").forEach { (label, icon) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(16.dp)).background(Panel)
                    .clickable { onNavigate(label) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, color = Blue, fontSize = 24.sp)
                Spacer(Modifier.width(14.dp))
                Text(label, color = Ice, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text("›", color = Blue, fontSize = 26.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("«Я верю, сегодня тебе повезёт…»", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun AboutScreen() {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("✧", color = Blue, fontSize = 54.sp)
        Text("AndroidRasa", color = Ice, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Версия 2.0", color = Blue, fontSize = 15.sp)
        Spacer(Modifier.height(14.dp))
        Text("Твой личный рандомайзер, созданный для тех, кто верит, что даже случайность может быть красивой.", color = Muted, textAlign = TextAlign.Center)
    }
}
