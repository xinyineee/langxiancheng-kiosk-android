package com.langxiancheng.kiosk.ui.screen.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Welcome/onboarding screen that introduces the test and prompts user to start.
 * Features staggered entrance animations and polished card design.
 *
 * @param onStartTest Callback when user taps "Start Test" button
 * @param onScreenVisible Callback when screen becomes visible (for TTS intro)
 */
@Composable
fun WelcomeScreen(
    onStartTest: () -> Unit,
    onScreenVisible: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
        onScreenVisible()
    }
    val visibleStep = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Staggered reveal animation
        visibleStep.intValue = 1
        kotlinx.coroutines.delay(200)
        visibleStep.intValue = 2
        kotlinx.coroutines.delay(200)
        visibleStep.intValue = 3
        kotlinx.coroutines.delay(200)
        visibleStep.intValue = 4
        kotlinx.coroutines.delay(200)
        visibleStep.intValue = 5
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWarm
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            // Brand logo
            AnimatedVisibility(
                visible = visibleStep.intValue >= 1,
                enter = fadeIn(tween(500)) + slideInVertically(tween(600)) { it / 2 }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_brand),
                    contentDescription = "浪险橙品牌Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            AnimatedVisibility(
                visible = visibleStep.intValue >= 2,
                enter = fadeIn(tween(500)) + slideInVertically(tween(600)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "你的创业咖啡口味",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = OrangePrimary,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "5 道趣味题，测出你的专属特调",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 17.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Feature cards
            AnimatedVisibility(
                visible = visibleStep.intValue >= 3,
                enter = fadeIn(tween(600)) + slideInVertically(tween(700)) { it / 4 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureRow(
                        icon = Icons.Filled.Timer,
                        title = "30秒快速测试",
                        subtitle = "5道题，轻松搞定"
                    )
                    FeatureRow(
                        icon = Icons.Filled.Coffee,
                        title = "6杯专属特调",
                        subtitle = "每一杯都是你的写照"
                    )
                    FeatureRow(
                        icon = Icons.Filled.Nfc,
                        title = "NFC一碰即传",
                        subtitle = "把结果分享给朋友"
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Start button
            AnimatedVisibility(
                visible = visibleStep.intValue >= 4,
                enter = fadeIn(tween(600)) + slideInVertically(tween(700)) { it / 5 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onStartTest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = "开始测试",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "每题限时 5 秒，超时自动选择",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(OrangePrimary.copy(alpha = 0.15f), OrangeLight.copy(alpha = 0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = OrangePrimary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
