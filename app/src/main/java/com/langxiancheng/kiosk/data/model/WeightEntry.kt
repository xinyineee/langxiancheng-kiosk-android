package com.langxiancheng.kiosk.data.model

/**
 * Represents a weight entry mapping an answer option to a drink with a score value.
 * Used in the scoring engine to accumulate drink scores.
 *
 * @property drinkId The target drink identifier (e.g., "D1")
 * @property weight The weight/score contribution (typically 1-3)
 */
data class WeightEntry(
    val drinkId: String,
    val weight: Int
)
