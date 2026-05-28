package com.langxiancheng.kiosk.ui.screen.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langxiancheng.kiosk.data.engine.TestEngine
import com.langxiancheng.kiosk.data.model.TestResult
import com.langxiancheng.kiosk.data.repository.TestDataRepository
import com.langxiancheng.kiosk.service.NfcWriteService
import com.langxiancheng.kiosk.service.TimerService
import com.langxiancheng.kiosk.ui.component.NfcWriteState
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
 * ViewModel managing the result screen state.
 * Handles:
 * - Loading the test result from the shared repository (set by QuestionViewModel)
 * - NFC tag writing
 * - Auto-return to idle after 15 seconds
 */
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: TestDataRepository,
    private val testEngine: TestEngine,
    private val nfcWriteService: NfcWriteService,
    private val timerService: TimerService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    /** The computed test result. */
    private var testResult: TestResult? = null

    /** Auto-return timer job. */
    private var idleTimerJob: Job? = null

    /**
     * Loads the test result from the shared repository.
     * Called when the result screen is first composed.
     * QuestionViewModel.finishTest() saves the result to repository.lastResult
     * before navigating here, so it should always be available.
     */
    fun loadResult() {
        val result = repository.lastResult
        if (result != null) {
            setTestResult(result)
        }
    }

    /**
     * Sets the test result for display and starts the idle timer.
     *
     * @param result The computed test result
     */
    fun setTestResult(result: TestResult) {
        testResult = result
        _uiState.update { it.copy(recommendedDrink = result.recommendedDrink) }
        startIdleTimer()
    }

    /**
     * Triggers writing the result URL to an NFC tag.
     */
    fun writeNfcTag() {
        val result = testResult ?: return
        _uiState.update { it.copy(nfcWriteState = NfcWriteState.WRITING) }

        val url = buildResultUrl(result)

        viewModelScope.launch {
            try {
                val success = nfcWriteService.writeNdefUrl(url)
                _uiState.update {
                    it.copy(nfcWriteState = if (success) NfcWriteState.SUCCESS else NfcWriteState.FAILURE)
                }

                // Reset NFC state after 3 seconds
                delay(3000L)
                _uiState.update { it.copy(nfcWriteState = NfcWriteState.IDLE) }
            } catch (e: Exception) {
                _uiState.update { it.copy(nfcWriteState = NfcWriteState.FAILURE) }
                delay(3000L)
                _uiState.update { it.copy(nfcWriteState = NfcWriteState.IDLE) }
            }
        }
    }

    /**
     * Builds the NFC result URL.
     * Format: https://cafe.langxiancheng.com/result?d={drinkId}&s={scoreHash}&t={timestamp}
     */
    private fun buildResultUrl(result: TestResult): String {
        return "https://cafe.langxiancheng.com/result?d=${result.recommendedDrink.id}&s=${result.scoreHash}&t=${result.timestamp}"
    }

    /**
     * Starts the 15-second idle timer that auto-returns to the idle screen.
     */
    private fun startIdleTimer() {
        idleTimerJob?.cancel()
        val startTime = System.currentTimeMillis()

        idleTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (RESULT_IDLE_TIMEOUT_MS - elapsed).coerceAtLeast(0L)

                _uiState.update { it.copy(remainingIdleTimeMs = remaining) }

                if (remaining <= 0L) {
                    break
                }
            }
        }
    }

    /**
     * Resets the idle timer (called on user interaction).
     */
    fun resetIdleTimer() {
        startIdleTimer()
    }

    /**
     * Returns whether the idle timer has expired.
     */
    fun isIdleTimerExpired(): Boolean {
        return _uiState.value.remainingIdleTimeMs <= 0L
    }

    /**
     * Clears the result from the repository (called when returning to idle).
     */
    fun clearResult() {
        repository.clearLastResult()
    }

    override fun onCleared() {
        super.onCleared()
        idleTimerJob?.cancel()
    }
}
