package com.langxiancheng.kiosk.ui.screen.result

import androidx.compose.runtime.Immutable
import com.langxiancheng.kiosk.data.model.Drink
import com.langxiancheng.kiosk.ui.component.NfcWriteState

/**
 * UI state for the Result screen.
 *
 * @property recommendedDrink The matched drink result
 * @property nfcWriteState Current NFC write state
 * @property remainingIdleTimeMs Time remaining before auto-return to idle (15s)
 */
@Immutable
data class ResultUiState(
    val recommendedDrink: Drink? = null,
    val nfcWriteState: NfcWriteState = NfcWriteState.IDLE,
    val remainingIdleTimeMs: Long = RESULT_IDLE_TIMEOUT_MS
)

/** Result screen auto-return timeout in milliseconds. */
const val RESULT_IDLE_TIMEOUT_MS = 30000L
