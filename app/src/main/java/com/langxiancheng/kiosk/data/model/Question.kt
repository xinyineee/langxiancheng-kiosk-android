package com.langxiancheng.kiosk.data.model

/**
 * Represents a quiz question in the personality test.
 *
 * @property id Unique question identifier (e.g., "Q1")
 * @property orderIndex Zero-based index for display ordering
 * @property questionText The full question text shown to the user
 * @property options List of answer options for this question
 */
data class Question(
    val id: String,
    val orderIndex: Int,
    val questionText: String,
    val options: List<AnswerOption>
)
