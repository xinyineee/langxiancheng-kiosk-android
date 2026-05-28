package com.langxiancheng.kiosk.ui.screen.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langxiancheng.kiosk.data.engine.TestEngine
import com.langxiancheng.kiosk.data.model.AnswerOption
import com.langxiancheng.kiosk.data.model.TestResult
import com.langxiancheng.kiosk.data.repository.TestDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the question screen state and test flow.
 * Handles:
 * - Loading questions from the repository
 * - Tracking selected answers
 * - Per-question countdown timer (5 seconds)
 * - Total test timeout (30 seconds)
 * - Auto-selection on timeout (defaults to option A)
 * - Advancing to the next question or completing the test
 * - Computing and persisting TestResult via repository for ResultViewModel
 */
@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: TestDataRepository,
    private val testEngine: TestEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    /** Map of question index to the selected AnswerOption. */
    private val selectedAnswers = mutableMapOf<Int, AnswerOption>()

    /** Active per-question countdown timer job. */
    private var timerJob: Job? = null

    /** Total test timeout timer job. */
    private var totalTimerJob: Job? = null

    /** Start time of the overall test (set when Q0 starts). */
    private var totalTestStartTime: Long = 0L

    /** Whether the total test timeout has been triggered. */
    private var isTotalTimedOut: Boolean = false

    /**
     * Initializes the question flow starting from the given index.
     *
     * @param questionIndex Zero-based question index (0-4)
     */
    fun startQuestion(questionIndex: Int) {
        timerJob?.cancel()

        val question = repository.questions.getOrNull(questionIndex) ?: return

        if (questionIndex == 0) {
            totalTestStartTime = System.currentTimeMillis()
            isTotalTimedOut = false
            startTotalTimer()
        }

        _uiState.update { currentState ->
            currentState.copy(
                currentQuestionIndex = questionIndex,
                totalQuestions = repository.getQuestionCount(),
                questionText = question.questionText,
                options = question.options.map { option ->
                    OptionUiModel(
                        label = option.label,
                        text = option.optionText,
                        isSelected = false
                    )
                },
                selectedOptionLabel = null,
                remainingTimeMs = SINGLE_QUESTION_TIMEOUT_MS,
                isTimedOut = false
            )
        }

        startCountdown()
    }

    /**
     * Handles the user selecting an answer option.
     *
     * @param optionLabel The label of the selected option (A/B/C/D)
     */
    fun selectOption(optionLabel: String) {
        val questionIndex = _uiState.value.currentQuestionIndex
        val question = repository.questions.getOrNull(questionIndex) ?: return
        val selectedOption = question.options.find { it.label == optionLabel } ?: return

        selectedAnswers[questionIndex] = selectedOption
        timerJob?.cancel()

        _uiState.update { currentState ->
            currentState.copy(
                selectedOptionLabel = optionLabel,
                options = currentState.options.map { opt ->
                    opt.copy(isSelected = opt.label == optionLabel)
                },
                remainingTimeMs = 0L
            )
        }
    }

    /**
     * Advances to the next question or signals test completion.
     * When the test is complete (all questions answered or total timeout),
     * computes the TestResult and saves it to the repository for ResultViewModel.
     *
     * @return Next question index, or -1 if the test is complete
     */
    fun advanceToNextQuestion(): Int {
        val currentIndex = _uiState.value.currentQuestionIndex
        val nextIndex = currentIndex + 1

        // Check if total time has expired — force finish regardless of remaining questions
        if (isTotalTimedOut || nextIndex >= repository.getQuestionCount()) {
            finishTest()
            return -1
        }

        startQuestion(nextIndex)
        return nextIndex
    }

    /**
     * Computes the final test result and saves it to the repository.
     * Called when all questions are answered OR when total timeout expires.
     */
    private fun finishTest() {
        totalTimerJob?.cancel()
        timerJob?.cancel()

        val result = testEngine.computeResult(selectedAnswers)
        repository.setLastResult(result)
    }

    /**
     * Computes the final test result using the scoring engine.
     *
     * @return TestResult with the recommended drink
     */
    fun computeTestResult(): TestResult {
        return testEngine.computeResult(selectedAnswers)
    }

    /**
     * Handles timeout for the current question.
     * Automatically selects option A as the default.
     */
    private fun handleTimeout() {
        val questionIndex = _uiState.value.currentQuestionIndex
        val question = repository.questions.getOrNull(questionIndex) ?: return

        // Default to option A on timeout
        val defaultOption = question.options.firstOrNull() ?: return
        selectedAnswers[questionIndex] = defaultOption

        _uiState.update { currentState ->
            currentState.copy(
                selectedOptionLabel = defaultOption.label,
                options = currentState.options.map { opt ->
                    opt.copy(isSelected = opt.label == defaultOption.label)
                },
                isTimedOut = true,
                remainingTimeMs = 0L
            )
        }
    }

    /**
     * Starts the per-question countdown timer.
     * Ticks every 100ms for smooth progress bar animation.
     */
    private fun startCountdown() {
        timerJob?.cancel()
        val startTime = System.currentTimeMillis()

        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_INTERVAL_MS)
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (SINGLE_QUESTION_TIMEOUT_MS - elapsed).coerceAtLeast(0L)

                _uiState.update { it.copy(remainingTimeMs = remaining) }

                if (remaining <= 0L) {
                    handleTimeout()
                    break
                }
            }
        }
    }

    /**
     * Starts the total test timeout timer (30 seconds).
     * If the total time expires, auto-selects defaults for unanswered questions
     * and forces navigation to the result screen.
     */
    private fun startTotalTimer() {
        totalTimerJob?.cancel()

        totalTimerJob = viewModelScope.launch {
            while (true) {
                delay(TOTAL_TIMER_TICK_MS)
                val elapsed = System.currentTimeMillis() - totalTestStartTime
                val remaining = (TOTAL_TEST_TIMEOUT_MS - elapsed).coerceAtLeast(0L)

                if (remaining <= 0L) {
                    isTotalTimedOut = true
                    handleTotalTimeout()
                    break
                }
            }
        }
    }

    /**
     * Handles total test timeout.
     * Auto-selects option A for all unanswered questions,
     * then forces navigation to the result screen.
     */
    private fun handleTotalTimeout() {
        timerJob?.cancel()

        // Auto-select option A for any unanswered questions
        val totalQuestions = repository.getQuestionCount()
        for (index in 0 until totalQuestions) {
            if (selectedAnswers[index] == null) {
                val question = repository.questions.getOrNull(index) ?: continue
                val defaultOption = question.options.firstOrNull() ?: continue
                selectedAnswers[index] = defaultOption
            }
        }

        // Update UI to show timeout state and trigger navigation
        _uiState.update { currentState ->
            currentState.copy(
                isTimedOut = true,
                remainingTimeMs = 0L,
                selectedOptionLabel = selectedAnswers[currentState.currentQuestionIndex]?.label
                    ?: currentState.selectedOptionLabel
            )
        }

        // Compute and save result immediately
        finishTest()
    }

    /**
     * Gets the currently selected answer option for the given question.
     */
    fun getSelectedAnswer(questionIndex: Int): AnswerOption? {
        return selectedAnswers[questionIndex]
    }

    /**
     * Returns whether the total test timeout has been triggered.
     */
    fun isTotalTimedOut(): Boolean = isTotalTimedOut

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        totalTimerJob?.cancel()
    }

    companion object {
        /** Single question timeout in milliseconds. */
        const val SINGLE_QUESTION_TIMEOUT_MS = 5000L

        /** Total test timeout in milliseconds. */
        const val TOTAL_TEST_TIMEOUT_MS = 30000L

        /** Per-question timer tick interval for smooth animation. */
        private const val TIMER_TICK_INTERVAL_MS = 100L

        /** Total timer tick interval. */
        private const val TOTAL_TIMER_TICK_MS = 500L
    }
}
