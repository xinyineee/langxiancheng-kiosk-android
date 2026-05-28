package com.langxiancheng.kiosk.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale

/**
 * A breathing/pulsing animation wrapper.
 * Applies a subtle scale and alpha animation to its content,
 * creating a "breathing" effect commonly used in kiosk attract screens.
 *
 * @param content The composable content to animate
 * @param modifier Optional modifier
 * @param minScale Minimum scale value (default 0.95)
 * @param maxScale Maximum scale value (default 1.05)
 * @param minAlpha Minimum alpha value (default 0.7f)
 * @param maxAlpha Maximum alpha value (default 1.0f)
 * @param durationMs Duration of one complete cycle in milliseconds
 */
@Composable
fun BreathAnimation(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    minAlpha: Float = 0.7f,
    maxAlpha: Float = 1.0f,
    durationMs: Int = 2000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")

    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs / 2),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs / 2),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_alpha"
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}
