package com.nuvio.tv.ui.components

import android.text.format.DateFormat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun CornerClock(modifier: Modifier = Modifier) {
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val formatter = remember(context) { DateFormat.getTimeFormat(context) }

    // Defer state update to next minute boundary for performance optimization
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            nowMillis = now
            val delayToNextMinute = 60000L - (now % 60000L)
            delay(delayToNextMinute)
        }
    }

    val shadowOffset = with(LocalDensity.current) { 2.dp.toPx() }

    Text(
        text = formatter.format(Date(nowMillis)),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.5f),
                offset = Offset(shadowOffset, shadowOffset),
                blurRadius = 0f
            )
        ),
        color = Color.White.copy(alpha = 0.95f),
        modifier = modifier
    )
}
