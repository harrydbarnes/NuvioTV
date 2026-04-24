package com.nuvio.tv.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date

@Composable
fun GlobalClockOverlay(
    modifier: Modifier = Modifier,
    paddingEnd: androidx.compose.ui.unit.Dp = 45.dp,
    paddingTop: androidx.compose.ui.unit.Dp = 40.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 27.sp,
    shadowAlpha: Float = 0.8f,
    shadowOffset: Float = 1.5f,
    shadowBlurRadius: Float = 0f
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val formatter = remember(context) { DateFormat.getTimeFormat(context) }

    // Check if the current time format contains seconds pattern
    val formatPattern = remember(formatter) {
        (formatter as? java.text.SimpleDateFormat)?.toPattern() ?: ""
    }
    val hasSeconds = formatPattern.contains("s")

    LaunchedEffect(hasSeconds) {
        while (true) {
            val currentMillis = System.currentTimeMillis()
            nowMillis = currentMillis

            val delayMs = if (hasSeconds) {
                // Delay to the next full second
                1000L - (currentMillis % 1000L)
            } else {
                // Delay to the next full minute
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = currentMillis
                val currentSeconds = calendar.get(Calendar.SECOND)
                val currentMillisOfSecond = calendar.get(Calendar.MILLISECOND)
                val msToNextMinute = ((60 - currentSeconds) * 1000L) - currentMillisOfSecond
                // Coerce at least to a reasonable minimum to avoid tight spinning just in case
                msToNextMinute.coerceAtLeast(100L)
            }
            delay(delayMs)
        }
    }

    val density = LocalDensity.current
    val scaledOffset = with(density) { shadowOffset.dp.toPx() }
    val scaledBlur = with(density) { shadowBlurRadius.dp.toPx() }

    Text(
        text = formatter.format(Date(nowMillis)),
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Normal,
            fontSize = fontSize,
            shadow = Shadow(
                color = Color.Black.copy(alpha = shadowAlpha),
                offset = Offset(scaledOffset, scaledOffset),
                blurRadius = scaledBlur
            )
        ),
        color = Color.White.copy(alpha = 0.95f),
        modifier = modifier.padding(end = paddingEnd, top = paddingTop)
    )
}
