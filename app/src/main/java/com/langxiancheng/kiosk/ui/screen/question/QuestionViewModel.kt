package com.langxiancheng.kiosk.ui.screen.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langxiancheng.kiosk.data.engine.TestEngine
import com.langxiancheng.kiosk.data.model.AnswerOption
import com.langxiancheng.kiosk.data.model.TestResult
import com.langxiancheng.kiosk.data.repository.TestDataRepository
import com.langxiancheng.kiosk.service.TtsService
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
 * ViewModel managing the question screen state, test flow, and voice interaction.
 * Handles:
 * - Loading questions from the repository
 * - Tracking selected answers
 * - Per-question countdown timer (5 seconds)
 * - Total test timeout (30 seconds)
 * - Auto-selection on timeout (defaults to option A)
 * - TTS voice reading of questions and options
 * - Advancing to the next question or completing the test
 * - Computing and persisting TestResult via repository for ResultViewModel
 */
@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: TestDataRepository,
    private val testEngine: TestEngine,
    private val ttsService: TtsService
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
     * Also triggers TTS to read the question aloud.
     */
    fun startQuestion(questionIndex: Int) {
        timerJob?.cancel()
        ttsService.stop()

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

        // Voice: read question and options
        speakCurrentQuestion()

        startCountdown()
    }

    /**
     * Reads the current question and all options via TTS.
     */
    fun speakCurrentQuestion() {
        val state = _uiState.value
        val question = repository.questions.getOrNull(state.currentQuestionIndex) ?: return
        val optionTexts = question.options.map { it.optionText }
        ttsService.speakQuestion(question.questionText, optionTexts)
    }

    /**
     * Handles the user selecting an answer option.
     * Also provides TTS feedback for the selection.
     */
    fun selectOption(optionLabel: String) {
        val questionIndex = _uiState.value.currentQuestionIndex
        val question = repository.questions.getOrNull(questionIndex) ?: return
        val selectedOption = question.options.find { it.label == optionLabel } ?: return

        selectedAnswers[questionIndex] = selectedOption
        timerJob?.cancel()

        // TTS feedback
        ttsService.speakSelectionFeedback(optionLabel)

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
     */
    fun advanceToNextQuestion(): Int {
        val currentIndex = _uiState.value.currentQuestionIndex
        val nextIndex = currentIndex + 1

        if (isTotalTimedOut || nextIndex >= repository.getQuestionCount()) {
            finishTest()
            return -1
        }

        startQuestion(nextIndex)
        return nextIndex
    }

    /**
     * Computes the final test result and saves it to the repository.
     */
    private fun finishTest() {
        totalTimerJob?.cancel()
        timerJob?.cancel()
        ttsService.stop()

        val result = testEngine.computeResult(selectedAnswers)
        repository.setLastResult(result)
    }

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

        val defaultOption = question.options.firstOrNull() ?: return
        selectedAnswers[questionIndex] = defaultOption

        // TTS: time warning
        ttsService.speakTimeWarning()

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
     */
    private fun handleTotalTimeout() {
        timerJob?.cancel()

        val totalQuestions = repository.getQuestionCount()
        for (index in 0 until totalQuestions) {
            if (selectedAnswers[index] == null) {
                val question = repository.questions.getOrNull(index) ?: continue
                val defaultOption = question.options.firstOrNull() ?: continue
                selectedAnswers[index] = defaultOption
            }
        }

        _uiState.update { currentState ->
            currentState.copy(
                isTimedOut = true,
                remainingTimeMs = 0L,
                selectedOptionLabel = selectedAnswers[currentState.currentQuestionIndex]?.label
                    ?: currentState.selectedOptionLabel
            )
        }

        finishTest()
    }

    fun getSelectedAnswer(questionIndex: Int): AnswerOption? {
        return selectedAnswers[questionIndex]
    }

    fun isTotalTimedOut(): Boolean = isTotalTimedOut

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        totalTimerJob?.cancel()
        ttsService.stop()
    }

    companion object {
        const val SINGLE_QUESTION_TIMEOUT_MS = 5000L
        const val TOTAL_TEST_TIMEOUT_MS = 30000L
        private const val TIMER_TICK_INTERVAL_MS = 100L
        private const val TOTAL_TIMER_TICK_MS = 500L
    }
}
