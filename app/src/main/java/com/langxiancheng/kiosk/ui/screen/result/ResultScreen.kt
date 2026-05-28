package com.langxiancheng.kiosk.ui.screen.result

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langxiancheng.kiosk.ui.component.NfcPromptCard
import com.langxiancheng.kiosk.ui.theme.BackgroundWarm
import com.langxiancheng.kiosk.ui.theme.OrangePrimary

/**
 * Result screen displaying the matched drink, heart copy, and NFC prompt.
 * Auto-returns to idle after 15 seconds of inactivity.
 *
 * @param onReturnToIdle Callback to navigate back to the idle screen
 */
@Composable
fun ResultScreen(
    onReturnToIdle: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drink = uiState.recommendedDrink

    // Load the test result from the shared repository when the screen first appears.
    // QuestionViewModel.finishTest() saves the result before navigating here.
    LaunchedEffect(Unit) {
        viewModel.loadResult()
    }

    // Wrapper that clears the result before navigating back to idle
    val returnToIdle = {
        viewModel.clearResult()
        onReturnToIdle()
    }

    // Auto-return to idle when timer expires
    LaunchedEffect(uiState.remainingIdleTimeMs) {
        if (uiState.remainingIdleTimeMs <= 0L) {
            returnToIdle()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWarm
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (drink != null) {
                // Drink result card
                ResultDrinkCard(drink = drink)

                Spacer(modifier = Modifier.height(20.dp))

                // Heart copy card
                HeartCopyCard(heartCopy = drink.heartCopy)

                Spacer(modifier = Modifier.height(24.dp))

                // NFC prompt card
                NfcPromptCard(
                    nfcWriteState = uiState.nfcWriteState,
                    onWriteNfc = { viewModel.writeNfcTag() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
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

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-return countdown indicator
                val idleSeconds = (uiState.remainingIdleTimeMs / 1000).toInt()
                Text(
                    text = "${idleSeconds}秒后返回首页",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Coffee,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OrangePrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在计算你的专属特调…",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displaying the recommended drink with its name, English name, and tagline.
 */
@Composable
private fun ResultDrinkCard(
    drink: com.langxiancheng.kiosk.data.model.Drink,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drink emoji/icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(OrangePrimary.copy(alpha = 0.15f), OrangePrimary.copy(alpha = 0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = drink.emoji,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chinese name
            Text(
                text = drink.name,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // English name
            Text(
                text = drink.englishName,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OrangePrimary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = drink.tagline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = OrangePrimary
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Card displaying the emotional/inspirational copy for the result.
 */
@Composable
private fun HeartCopyCard(
    heartCopy: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "「",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = OrangePrimary.copy(alpha = 0.4f)
                )
            )
            Text(
                text = heartCopy,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                ),
                textAlign = TextAlign.Start
            )
            Text(
                text = "」",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = OrangePrimary.copy(alpha = 0.4f)
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
