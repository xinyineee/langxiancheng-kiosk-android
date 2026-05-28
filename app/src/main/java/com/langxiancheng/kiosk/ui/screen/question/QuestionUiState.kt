package com.langxiancheng.kiosk.ui.screen.question

import androidx.compose.runtime.Immutable

/**
 * UI state for the Question screen.
 *
 * @property currentQuestionIndex Zero-based index of the current question (0-4)
 * @property totalQuestions Total number of questions (5)
 * @property questionText The text of the current question
 * @property options List of available answer options for the current question
 * @property selectedOptionLabel Currently selected option label (null if none selected)
 * @property remainingTimeMs Remaining time in milliseconds for the current question
 * @property isTimedOut Whether the current question has timed out
 */
@Immutable
data class QuestionUiState(
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 5,
    val questionText: String = "",
    val options: List<OptionUiModel> = emptyList(),
    val selectedOptionLabel: String? = null,
    val remainingTimeMs: Long = 5000L,
    val isTimedOut: Boolean = false
)

/**
 * UI model for a single answer option displayed on the question screen.
 *
 * @property label Display label (A, B, C, D)
 * @property text Full option text
 * @property isSelected Whether this option is currently selected
 */
@Immutable
data class OptionUiModel(
    val label: String,
    val text: String,
    val isSelected: Boolean = false
)
