package com.langxiancheng.kiosk.ui.screen.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langxiancheng.kiosk.ui.component.NfcPromptCard
import com.langxiancheng.kiosk.ui.theme.BackgroundWarm
import com.langxiancheng.kiosk.ui.theme.OrangePrimary

/**
 * Result screen displaying the matched drink with illustration, heart copy, and NFC prompt.
 * Features large drink image, staggered entrance animations, and voice playback.
 * Auto-returns to idle after 30 seconds of inactivity.
 */
@Composable
fun ResultScreen(
    onReturnToIdle: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drink = uiState.recommendedDrink
    val scrollState = rememberScrollState()
    val visibleStep = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadResult()
        // Staggered reveal
        visibleStep.intValue = 1
        kotlinx.coroutines.delay(300)
        visibleStep.intValue = 2
        kotlinx.coroutines.delay(300)
        visibleStep.intValue = 3
        kotlinx.coroutines.delay(300)
        visibleStep.intValue = 4
    }

    val returnToIdle = {
        viewModel.clearResult()
        onReturnToIdle()
    }

    LaunchedEffect(uiState.remainingIdleTimeMs) {
        if (uiState.remainingIdleTimeMs <= 0L) {
            returnToIdle()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWarm
    ) {
        if (drink != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header label
                AnimatedVisibility(
                    visible = visibleStep.intValue >= 1,
                    enter = fadeIn(tween(500))
                ) {
                    Text(
                        text = "你的专属特调",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Drink image card with large illustration
                AnimatedVisibility(
                    visible = visibleStep.intValue >= 1,
                    enter = fadeIn(tween(700)) + scaleIn(tween(700), initialScale = 0.85f)
                ) {
                    DrinkImageCard(drink = drink)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Heart copy card
                AnimatedVisibility(
                    visible = visibleStep.intValue >= 2,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
                ) {
                    HeartCopyCard(heartCopy = drink.heartCopy)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // NFC prompt
                AnimatedVisibility(
                    visible = visibleStep.intValue >= 3,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
                ) {
                    NfcPromptCard(
                        nfcWriteState = uiState.nfcWriteState,
                        onWriteNfc = { viewModel.writeNfcTag() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                AnimatedVisibility(
                    visible = visibleStep.intValue >= 4,
                    enter = fadeIn(tween(600))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = returnToIdle,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OrangePrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("再测一次")
                        }

                        Button(
                            onClick = { viewModel.writeNfcTag() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Nfc,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("写入NFC")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Voice replay button
                AnimatedVisibility(
                    visible = visibleStep.intValue >= 4,
                    enter = fadeIn(tween(600))
                ) {
                    IconButton(
                        onClick = { viewModel.speakResultAgain() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "重新播报",
                                tint = OrangePrimary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "重新播报",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OrangePrimary.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Auto-return countdown
                val idleSeconds = (uiState.remainingIdleTimeMs / 1000).toInt()
                Text(
                    text = "${idleSeconds}秒后返回首页",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "loading_scale"
                    )
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☕",
                            fontSize = 40.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "正在调配你的专属特调…",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DrinkImageCard(
    drink: com.langxiancheng.kiosk.data.model.Drink,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            OrangePrimary.copy(alpha = glowAlpha),
                            OrangePrimary.copy(alpha = 0.01f)
                        )
                    )
                )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large drink illustration
                drink.drawableResId?.let { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = drink.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } ?: run {
                    // Fallback emoji display
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        OrangePrimary.copy(alpha = 0.15f),
                                        OrangePrimary.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = drink.emoji,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drink name
                Text(
                    text = drink.name,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = OrangePrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // English name
                Text(
                    text = drink.englishName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tagline badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OrangePrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = drink.tagline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = OrangePrimary,
                            fontSize = 15.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun HeartCopyCard(
    heartCopy: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "「",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = OrangePrimary.copy(alpha = 0.35f),
                    fontSize = 32.sp
                )
            )
            Text(
                text = heartCopy,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 28.sp,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                ),
                textAlign = TextAlign.Start
            )
            Text(
                text = "」",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = OrangePrimary.copy(alpha = 0.35f),
                    fontSize = 32.sp
                ),
                modifier = Modifier.align(Alignment.End)
            )
            Text(
                text = "— 浪险橙咖啡",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 12.sp
                ),
                modifier = Modifier.align(Alignment.End),
                textAlign = TextAlign.End
            )
        }
    }
}
