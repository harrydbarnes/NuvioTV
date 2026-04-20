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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun GlobalClockOverlay(modifier: Modifier = Modifier) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val formatter = remember(context) { DateFormat.getTimeFormat(context) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }

    Text(
        text = formatter.format(Date(nowMillis)),
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp
        ),
        color = Color.White.copy(alpha = 0.95f),
        modifier = modifier.padding(end = 56.dp, top = 40.dp)
    )
}
