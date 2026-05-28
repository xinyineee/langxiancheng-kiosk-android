package com.langxiancheng.kiosk.ui.screen.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langxiancheng.kiosk.ui.component.CountdownTimerBar
import com.langxiancheng.kiosk.ui.component.OptionCard
import com.langxiancheng.kiosk.ui.component.ProgressIndicator
import com.langxiancheng.kiosk.ui.theme.BackgroundWarm

/**
 * Question screen displaying the current quiz question, options, and countdown timer.
 *
 * @param questionIndex Zero-based question index passed via navigation
 * @param onNextQuestion Callback to advance to the next question (passes next index)
 * @param onFinishTest Callback when all questions are answered
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

    // Load question when the screen is first displayed
    LaunchedEffect(questionIndex) {
        viewModel.startQuestion(questionIndex)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundWarm
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            // Progress indicator
            ProgressIndicator(
                currentIndex = uiState.currentQuestionIndex,
                totalCount = uiState.totalQuestions
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Countdown timer bar
            CountdownTimerBar(
                remainingMs = uiState.remainingTimeMs,
                totalMs = QuestionViewModel.SINGLE_QUESTION_TIMEOUT_MS
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Question number label
            Text(
                text = "第 ${uiState.currentQuestionIndex + 1} 题",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Question text
            Text(
                text = uiState.questionText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Timeout hint
            if (uiState.isTimedOut) {
                Text(
                    text = "时间到，已自动选择",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Options
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                uiState.options.forEach { option ->
                    OptionCard(
                        label = option.label,
                        text = option.text,
                        isSelected = option.isSelected,
                        onClick = {
                            if (uiState.selectedOptionLabel == null) {
                                viewModel.selectOption(option.label)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-advance after selection with brief delay for visual feedback
            LaunchedEffect(uiState.selectedOptionLabel) {
                if (uiState.selectedOptionLabel != null) {
                    kotlinx.coroutines.delay(800L) // Brief pause for user to see selection
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
