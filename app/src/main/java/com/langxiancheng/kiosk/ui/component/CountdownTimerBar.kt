package com.langxiancheng.kiosk.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.langxiancheng.kiosk.ui.theme.OrangeLight
import com.langxiancheng.kiosk.ui.theme.OrangePrimary

/**
 * A horizontal countdown timer progress bar.
 * Animates from full to empty over the specified duration.
 *
 * @param remainingMs Remaining time in milliseconds
 * @param totalMs Total time in milliseconds
 * @param modifier Optional modifier
 * @param trackColor Background track color
 * @param indicatorColor Foreground progress color
 */
@Composable
fun CountdownTimerBar(
    remainingMs: Long,
    totalMs: Long,
    modifier: Modifier = Modifier,
    trackColor: Color = OrangeLight.copy(alpha = 0.3f),
    indicatorColor: Color = OrangePrimary
) {
    val progress = if (totalMs > 0L) {
        (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "countdown_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(indicatorColor)
        )
    }
}
