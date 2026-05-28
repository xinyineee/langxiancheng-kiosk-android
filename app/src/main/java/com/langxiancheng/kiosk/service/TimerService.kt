package com.langxiancheng.kiosk.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global timer service for managing test timing.
 *
 * Provides:
 * - Total test timer (30 seconds)
 * - Per-question timer (5 seconds)
 * - Result page idle timer (15 seconds)
 * - Timer state observation via StateFlow
 */
@Singleton
class TimerService @Inject constructor() {

    private val _totalRemainingMs = MutableStateFlow(TOTAL_TEST_TIMEOUT_MS)
    val totalRemainingMs: StateFlow<Long> = _totalRemainingMs.asStateFlow()

    private val _questionRemainingMs = MutableStateFlow(SINGLE_QUESTION_TIMEOUT_MS)
    val questionRemainingMs: StateFlow<Long> = _questionRemainingMs.asStateFlow()

    private val _isTestTimerRunning = MutableStateFlow(false)
    val isTestTimerRunning: StateFlow<Boolean> = _isTestTimerRunning.asStateFlow()

    private var totalTimerJob: Job? = null
    private var questionTimerJob: Job? = null

    /**
     * Starts the global test timer (30 seconds total).
     * If the timer expires, the test is considered timed out.
     */
    fun startTotalTimer() {
        stopTotalTimer()
        _isTestTimerRunning.value = true
        val startTime = System.currentTimeMillis()

        totalTimerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(TIMER_TICK_MS)
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (TOTAL_TEST_TIMEOUT_MS - elapsed).coerceAtLeast(0L)
                _totalRemainingMs.value = remaining

                if (remaining <= 0L) {
                    _isTestTimerRunning.value = false
                    break
                }
            }
        }
    }

    /**
     * Stops the global test timer.
     */
    fun stopTotalTimer() {
        totalTimerJob?.cancel()
        _isTestTimerRunning.value = false
    }

    /**
     * Starts a single question timer (5 seconds).
     *
     * @param onTimeout Callback when the question timer expires
     */
    fun startQuestionTimer(onTimeout: () -> Unit) {
        stopQuestionTimer()
        _questionRemainingMs.value = SINGLE_QUESTION_TIMEOUT_MS
        val startTime = System.currentTimeMillis()

        questionTimerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(TIMER_TICK_MS)
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (SINGLE_QUESTION_TIMEOUT_MS - elapsed).coerceAtLeast(0L)
                _questionRemainingMs.value = remaining

                if (remaining <= 0L) {
                    onTimeout()
                    break
                }
            }
        }
    }

    /**
     * Stops the current question timer.
     */
    fun stopQuestionTimer() {
        questionTimerJob?.cancel()
    }

    /**
     * Resets all timers to their initial state.
     */
    fun resetAll() {
        stopTotalTimer()
        stopQuestionTimer()
        _totalRemainingMs.value = TOTAL_TEST_TIMEOUT_MS
        _questionRemainingMs.value = SINGLE_QUESTION_TIMEOUT_MS
    }

    companion object {
        /** Total test timeout in milliseconds. */
        const val TOTAL_TEST_TIMEOUT_MS = 30000L

        /** Single question timeout in milliseconds. */
        const val SINGLE_QUESTION_TIMEOUT_MS = 5000L

        /** Result page idle timeout in milliseconds. */
        const val RESULT_IDLE_TIMEOUT_MS = 15000L

        /** Timer tick interval for smooth animation. */
        private const val TIMER_TICK_MS = 100L

        private const val TAG = "TimerService"
    }
}
