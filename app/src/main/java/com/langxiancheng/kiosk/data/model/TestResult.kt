package com.langxiancheng.kiosk.data.model

/**
 * Represents the result of a completed personality test.
 *
 * @property recommendedDrink The matched drink based on scoring
 * @property scores Map of drink IDs to their accumulated scores
 * @property selectedAnswers Map of question index to selected option label
 * @property scoreHash Hash for URL verification (first 6 chars of MD5)
 * @property timestamp Unix timestamp of when the test was completed
 */
data class TestResult(
    val recommendedDrink: Drink,
    val scores: Map<String, Int>,
    val selectedAnswers: Map<Int, String>,
    val scoreHash: String,
    val timestamp: Long = System.currentTimeMillis()
)
