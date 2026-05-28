package com.langxiancheng.kiosk.data.model

/**
 * Represents a single answer option for a quiz question.
 *
 * @property label Display label (e.g., "A", "B", "C", "D")
 * @property optionText The full option text shown to the user
 * @property weights List of weight entries that map to drink scores
 */
data class AnswerOption(
    val label: String,
    val optionText: String,
    val weights: List<WeightEntry>
)
