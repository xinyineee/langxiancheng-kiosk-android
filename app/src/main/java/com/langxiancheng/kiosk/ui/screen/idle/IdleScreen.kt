package com.langxiancheng.kiosk.ui.screen.idle

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.langxiancheng.kiosk.R
import com.langxiancheng.kiosk.ui.theme.BackgroundWarm
import com.langxiancheng.kiosk.ui.theme.OrangeDark
import com.langxiancheng.kiosk.ui.theme.OrangeLight
import com.langxiancheng.kiosk.ui.theme.OrangePrimary

/**
 * Idle/attract screen shown when no user is interacting with the kiosk.
 * Features brand logo breathing animation and touch-to-start prompt.
 *
 * @param onStartClicked Callback when user touches to start the test
 * @param onScreenVisible Callback when screen becomes visible (for TTS welcome)
 */
@Composable
fun IdleScreen(
    onStartClicked: () -> Unit,
    onScreenVisible: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onScreenVisible()
    }
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStartClicked
            ),
        color = BackgroundWarm
    ) {
        // Subtle decorative background elements
        Box(modifier = Modifier.fillMaxSize()) {
            // Top-right decorative circle
            DecorativeCircle(
                size = 180.dp,
                alpha = 0.06f,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            // Bottom-left decorative circle
            DecorativeCircle(
                size = 220.dp,
                alpha = 0.04f,
                modifier = Modifier.align(Alignment.BottomStart)
            )
            // Top-left small circle
            DecorativeCircle(
                size = 80.dp,
                alpha = 0.05f,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 60.dp, start = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.8f))

                // Brand logo with breathing glow
                BrandLogoWithGlow(
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Brand name
                Text(
                    text = "浪险橙咖啡",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = OrangePrimary,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Slogan with subtle styling
                Text(
                    text = "我们一起浪，一起险，一起成",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(56.dp))

                // Touch prompt with breathing animation
                TouchPrompt()

                Spacer(modifier = Modifier.weight(1f))

                // Bottom tagline card
                Surface(
                    modifier = Modifier.width(300.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "测测你是哪杯特调？",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = OrangePrimary,
                                fontSize = 22.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "5 道趣味题 · 6 杯专属特调 · 30 秒完成",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun BrandLogoWithGlow(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_breath")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_alpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow background
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .alpha(alpha * 0.3f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            OrangePrimary.copy(alpha = 0.4f),
                            OrangePrimary.copy(alpha = 0.0f)
                        )
                    )
                )
        )
        // Brand logo image
        Image(
            painter = painterResource(id = R.drawable.logo_brand),
            contentDescription = "浪险橙品牌Logo",
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun TouchPrompt() {
    val infiniteTransition = rememberInfiniteTransition(label = "touch_breath")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "touch_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "touch_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Icon(
            imageVector = Icons.Filled.TouchApp,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .scale(scale),
            tint = OrangePrimary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "触摸屏幕开始测试",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DecorativeCircle(
    size: androidx.compose.ui.unit.Dp,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "decorative_circle")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = alpha,
        targetValue = alpha * 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circle_alpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangeLight.copy(alpha = animatedAlpha),
                        OrangeLight.copy(alpha = 0f)
                    )
                )
            )
    )
}
