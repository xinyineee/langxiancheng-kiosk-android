package com.langxiancheng.kiosk.ui.screen.question

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langxiancheng.kiosk.ui.component.CountdownTimerBar
import com.langxiancheng.kiosk.ui.component.OptionCard
import com.langxiancheng.kiosk.ui.component.ProgressIndicator
import com.langxiancheng.kiosk.ui.theme.BackgroundWarm
import com.langxiancheng.kiosk.ui.theme.OrangePrimary

/**
 * Question screen with polished UI and voice interaction.
 * Features enhanced visual design, TTS question reading, and smooth transitions.
 */
@Composable
fun QuestionScreen(
    questionIndex: Int,
    onNextQuestion: (Int) -> Unit,
    onFinishTest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuestionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var direction by remember { mutableStateOf(1) }

    LaunchedEffect(questionIndex) {
        direction = 1
        viewModel.startQuestion(questionIndex)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWarm
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            // Top bar: progress + voice replay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressIndicator(
                    currentIndex = uiState.currentQuestionIndex,
                    totalCount = uiState.totalQuestions
                )

                IconButton(
                    onClick = { viewModel.speakCurrentQuestion() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "朗读题目",
                        tint = OrangePrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer bar
            CountdownTimerBar(
                remainingMs = uiState.remainingTimeMs,
                totalMs = QuestionViewModel.SINGLE_QUESTION_TIMEOUT_MS
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Question content with slide animation
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(tween(400)) { it / 4 } + fadeIn(tween(400)),
                exit = slideOutHorizontally(tween(300)) { -it / 4 }
            ) {
                QuestionContent(
                    uiState = uiState,
                    onSelectOption = { label ->
                        if (uiState.selectedOptionLabel == null) {
                            viewModel.selectOption(label)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-advance after selection
            LaunchedEffect(uiState.selectedOptionLabel) {
                if (uiState.selectedOptionLabel != null) {
                    kotlinx.coroutines.delay(700L)
                    val nextIndex = viewModel.advanceToNextQuestion()
                    if (nextIndex == -1) {
                        onFinishTest()
                    } else {
                        onNextQuestion(nextIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionContent(
    uiState: QuestionUiState,
    onSelectOption: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Question label
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = OrangePrimary.copy(alpha = 0.1f)
        ) {
            Text(
                text = "第 ${uiState.currentQuestionIndex + 1} 题",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = OrangePrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question text with larger font
        Text(
            text = uiState.questionText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 32.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Question hint
        Text(
            text = "选择最符合你此刻状态的答案",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Timeout hint
        if (uiState.isTimedOut) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Text(
                    text = "⏱ 时间到，已自动选择",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Options with spacing
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            uiState.options.forEach { option ->
                OptionCard(
                    label = option.label,
                    text = option.text,
                    isSelected = option.isSelected,
                    onClick = { onSelectOption(option.label) }
                )
            }
        }
    }
}
