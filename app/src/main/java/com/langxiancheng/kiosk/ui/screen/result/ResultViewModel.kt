package com.langxiancheng.kiosk.ui.screen.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langxiancheng.kiosk.data.engine.TestEngine
import com.langxiancheng.kiosk.data.model.TestResult
import com.langxiancheng.kiosk.data.repository.TestDataRepository
import com.langxiancheng.kiosk.service.NfcWriteService
import com.langxiancheng.kiosk.service.TimerService
import com.langxiancheng.kiosk.service.TtsService
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
 * ViewModel managing the result screen state with TTS voice feedback.
 *
 * NFC Flow:
 * 1. User taps "写入NFC" button → [writeNfcTag] calls [NfcWriteService.prepareWrite]
 * 2. [NfcWriteService.writeState] becomes WRITING → UI shows "请将手机靠近NFC标签"
 * 3. MainActivity discovers NFC tag → calls [NfcWriteService.writePendingTag]
 * 4. [NfcWriteService.writeState] becomes SUCCESS/FAILURE → UI updates + TTS feedback
 * 5. After 3 seconds, state resets to IDLE
 */
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: TestDataRepository,
    private val testEngine: TestEngine,
    private val nfcWriteService: NfcWriteService,
    private val timerService: TimerService,
    private val ttsService: TtsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    /** The computed test result. */
    private var testResult: TestResult? = null

    /** Auto-return timer job. */
    private var idleTimerJob: Job? = null

    /** Whether TTS has already announced the result. */
    private var hasAnnouncedResult: Boolean = false

    /** State reset job for NFC feedback timeout. */
    private var nfcStateResetJob: Job? = null

    init {
        // Observe NFC write state from NfcWriteService
        viewModelScope.launch {
            nfcWriteService.writeState.collect { state ->
                when (state) {
                    NfcWriteState.SUCCESS -> {
                        ttsService.speakNfcSuccess()
                        scheduleNfcStateReset()
                    }
                    NfcWriteState.FAILURE -> {
                        ttsService.speakNfcFailed()
                        scheduleNfcStateReset()
                    }
                    else -> {}
                }
                _uiState.update { it.copy(nfcWriteState = state) }
            }
        }
    }

    /**
     * Loads the test result from the shared repository.
     * Triggers TTS announcement on first load.
     */
    fun loadResult() {
        val result = repository.lastResult
        if (result != null) {
            setTestResult(result)
        }
    }

    /**
     * Sets the test result for display, starts idle timer, and announces via TTS.
     */
    fun setTestResult(result: TestResult) {
        testResult = result
        _uiState.update { it.copy(recommendedDrink = result.recommendedDrink) }
        startIdleTimer()

        if (!hasAnnouncedResult) {
            hasAnnouncedResult = true
            val drink = result.recommendedDrink
            ttsService.speakResult(drink.name, drink.tagline, drink.heartCopy)
        }
    }

    /**
     * Replays the result announcement via TTS.
     */
    fun speakResultAgain() {
        val result = testResult ?: return
        val drink = result.recommendedDrink
        ttsService.speakResult(drink.name, drink.tagline, drink.heartCopy)
    }

    /**
     * Triggers NFC write preparation.
     * Sets up the URL and enters waiting-for-tap state.
     * Actual write happens when a tag is discovered (handled by MainActivity).
     */
    fun writeNfcTag() {
        val result = testResult ?: return

        // Cancel any previous NFC state reset
        nfcStateResetJob?.cancel()

        val url = buildResultUrl(result)
        nfcWriteService.prepareWrite(url)
    }

    /**
     * Cancels the pending NFC write and resets state.
     */
    fun cancelNfcWrite() {
        nfcStateResetJob?.cancel()
        nfcWriteService.cancelPendingWrite()
        _uiState.update { it.copy(nfcWriteState = NfcWriteState.IDLE) }
    }

    /**
     * Schedules a reset of the NFC write state to IDLE after 3 seconds.
     */
    private fun scheduleNfcStateReset() {
        nfcStateResetJob?.cancel()
        nfcStateResetJob = viewModelScope.launch {
            delay(3000L)
            nfcWriteService.resetWriteState()
            _uiState.update { it.copy(nfcWriteState = NfcWriteState.IDLE) }
        }
    }

    /**
     * Builds the NFC result URL.
     */
    private fun buildResultUrl(result: TestResult): String {
        return "https://cafe.langxiancheng.com/result?d=${result.recommendedDrink.id}&s=${result.scoreHash}&t=${result.timestamp}"
    }

    /**
     * Starts the 30-second idle timer that auto-returns to the idle screen.
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

    fun resetIdleTimer() {
        startIdleTimer()
    }

    fun isIdleTimerExpired(): Boolean {
        return _uiState.value.remainingIdleTimeMs <= 0L
    }

    fun clearResult() {
        repository.clearLastResult()
        ttsService.stop()
        hasAnnouncedResult = false
    }

    override fun onCleared() {
        super.onCleared()
        idleTimerJob?.cancel()
        nfcStateResetJob?.cancel()
        ttsService.stop()
    }

    companion object {
        /** Result screen idle timeout in milliseconds (30 seconds). */
        const val RESULT_IDLE_TIMEOUT_MS = 30000L
    }
}
