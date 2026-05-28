package com.langxiancheng.kiosk.ui.component

/**
 * State of NFC tag writing operation.
 * Shared across components to avoid cross-package coupling.
 */
enum class NfcWriteState {
    /** Not writing, waiting for user action. */
    IDLE,
    /** Currently writing to NFC tag. */
    WRITING,
    /** Write completed successfully. */
    SUCCESS,
    /** Write failed. */
    FAILURE
}
