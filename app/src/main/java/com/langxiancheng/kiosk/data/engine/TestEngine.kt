package com.langxiancheng.kiosk.data.engine

import com.langxiancheng.kiosk.data.model.AnswerOption
import com.langxiancheng.kiosk.data.model.Drink
import com.langxiancheng.kiosk.data.model.TestResult
import com.langxiancheng.kiosk.data.repository.TestDataRepository
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core scoring engine for the personality test.
 * Implements weight accumulation and drink matching logic.
 *
 * Algorithm:
 * 1. Initialize score map with all drink IDs set to 0
 * 2. For each selected answer, accumulate its weight entries
 * 3. Find the drink with the highest accumulated score
 * 4. In case of tie, select the drink with the larger ID
 * 5. Generate scoreHash for NFC URL verification
 */
@Singleton
class TestEngine @Inject constructor(
    private val repository: TestDataRepository
) {

    /**
     * Computes the test result based on the user's selected answers.
     *
     * @param selectedOptions Map of question index (0-based) to the selected AnswerOption
     * @return TestResult containing the recommended drink and scoring details
     */
    fun computeResult(selectedOptions: Map<Int, AnswerOption>): TestResult {
        val scores = initializeScores()

        // Accumulate weights from each selected answer
        for ((_, option) in selectedOptions) {
            for (weightEntry in option.weights) {
                val currentScore = scores[weightEntry.drinkId] ?: 0
                scores[weightEntry.drinkId] = currentScore + weightEntry.weight
            }
        }

        // Find the best matching drink (highest score, tie-break by larger ID)
        val bestDrinkId = findBestDrinkId(scores)
        val recommendedDrink = repository.getDrinkById(bestDrinkId)
            ?: repository.drinks.first()

        // Generate hash for URL verification
        val scoreHash = generateScoreHash(scores)
        val selectedAnswers = selectedOptions.mapValues { it.value.label }

        return TestResult(
            recommendedDrink = recommendedDrink,
            scores = scores,
            selectedAnswers = selectedAnswers,
            scoreHash = scoreHash,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Initializes a score map with all drink IDs set to 0.
     */
    private fun initializeScores(): MutableMap<String, Int> {
        return repository.drinks.associate { it.id to 0 }.toMutableMap()
    }

    /**
     * Finds the drink ID with the highest accumulated score.
     * Tie-breaking rule: select the drink with the larger ID.
     *
     * @param scores Map of drink ID to accumulated score
     * @return The winning drink ID
     */
    private fun findBestDrinkId(scores: Map<String, Int>): String {
        var bestId = "D1"
        var bestScore = -1

        for ((drinkId, score) in scores) {
            if (score > bestScore || (score == bestScore && drinkId > bestId)) {
                bestScore = score
                bestId = drinkId
            }
        }

        return bestId
    }

    /**
     * Generates a score hash for NFC URL verification.
     * Format: MD5(scores.toString() + "lxc2026").substring(0, 6)
     *
     * @param scores Map of drink ID to accumulated score
     * @return First 6 characters of the MD5 hash
     */
    private fun generateScoreHash(scores: Map<String, Int>): String {
        val input = scores.toString() + "lxc2026"
        val md5Bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        val hexString = md5Bytes.joinToString("") { "%02x".format(it) }
        return hexString.substring(0, 6)
    }

    companion object {
        private const val TAG = "TestEngine"
        private const val HASH_SALT = "lxc2026"
        private const val HASH_LENGTH = 6
    }
}
