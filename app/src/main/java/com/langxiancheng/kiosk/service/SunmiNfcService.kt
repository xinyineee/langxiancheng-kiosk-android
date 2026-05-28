package com.langxiancheng.kiosk.service

import android.content.Context
import android.util.Log
import com.sunmi.nfc.INfcListener
import com.sunmi.nfc.Nfc
import com.sunmi.peripheralsdk.NfcManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for controlling the FLEX 3 under-screen NFC hardware module
 * using the Sunmi Peripheral SDK.
 *
 * Responsibilities:
 * - Initialize and bind to Sunmi's peripheral manager service
 * - Switch to under-screen NFC mode (FLEX 3 specific)
 * - Control NFC watermark transparency (the visual indicator on screen)
 * - Monitor NFC device list changes via INfcListener
 *
 * This service handles ONLY hardware module control.
 * Actual NDEF tag read/write is handled by [NfcWriteService] using standard Android NFC API.
 */
@Singleton
class SunmiNfcService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Sunmi NfcManager singleton for hardware control. */
    private val nfcManager = NfcManager

    /** Whether the service is successfully initialized and bound. */
    var isInitialized: Boolean = false
        private set

    /** Whether under-screen NFC is currently active. */
    var isUnderScreenNfcActive: Boolean = false
        private set

    /**
     * Initializes the Sunmi NFC service.
     * Binds to the peripheral manager and switches to under-screen NFC mode.
     * Must be called from main thread.
     *
     * @param callback Called with true if initialization succeeded, false otherwise
     */
    fun initialize(callback: (Boolean) -> Unit) {
        Log.d(TAG, "Initializing Sunmi NFC service...")

        try {
            nfcManager.init(context) { success ->
                isInitialized = success
                if (success) {
                    Log.d(TAG, "Sunmi NFC service bound successfully")
                    // Switch to under-screen NFC mode
                    switchToUnderScreenNfc()
                    // Register listener for NFC device changes
                    registerNfcListener()
                } else {
                    Log.e(TAG, "Failed to bind Sunmi NFC service")
                }
                callback(success)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception initializing Sunmi NFC service", e)
            isInitialized = false
            callback(false)
        }
    }

    /**
     * Switches to under-screen NFC mode on FLEX 3.
     * This enables the NFC antenna embedded under the display.
     */
    private fun switchToUnderScreenNfc() {
        try {
            // "under_screen" tells FLEX 3 to use the built-in under-screen NFC module
            val result = nfcManager.switchNfc(NFC_TYPE_UNDER_SCREEN)
            isUnderScreenNfcActive = result
            Log.d(TAG, "Switch to under-screen NFC: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch NFC module", e)
            isUnderScreenNfcActive = false
        }
    }

    /**
     * Switches to external NFC module (e.g., USB-connected NFC reader).
     */
    fun switchToExternalNfc() {
        try {
            val result = nfcManager.switchNfc(NFC_TYPE_EXTERNAL)
            isUnderScreenNfcActive = !result
            Log.d(TAG, "Switch to external NFC: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch NFC module", e)
        }
    }

    /**
     * Sets the NFC watermark transparency.
     * The watermark is the visual indicator shown on screen when NFC is active.
     *
     * @param alpha Transparency level (0 = fully transparent, 255 = fully opaque)
     */
    fun setWatermarkAlpha(alpha: Int) {
        try {
            nfcManager.setNfcWaterMarkAlpha(alpha)
            Log.d(TAG, "Watermark alpha set to: $alpha")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set watermark alpha", e)
        }
    }

    /**
     * Registers a listener for NFC device list changes.
     * Called automatically during initialization.
     */
    private fun registerNfcListener() {
        try {
            val listener = object : INfcListener.Stub() {
                override fun onNfcListChanged(nfcList: MutableList<Nfc>?) {
                    val count = nfcList?.size ?: 0
                    Log.d(TAG, "NFC device list changed. Devices: $count")
                    nfcList?.forEach { nfc ->
                        Log.d(TAG, "  NFC device: type=${nfc.type}, selected=${nfc.isSelect}, watermarkAlpha=${nfc.waterMarkAlpha}")
                    }
                }
            }
            nfcManager.registerNfcListener(listener)
            Log.d(TAG, "NFC listener registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register NFC listener", e)
        }
    }

    /**
     * Releases the Sunmi NFC service and unbinds from peripheral manager.
     */
    fun release() {
        try {
            nfcManager.unregisterNfcListener()
            nfcManager.destroy(context)
            isInitialized = false
            isUnderScreenNfcActive = false
            Log.d(TAG, "Sunmi NFC service released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing Sunmi NFC service", e)
        }
    }

    companion object {
        private const val TAG = "SunmiNfcService"

        /** Under-screen NFC module type identifier for FLEX 3. */
        private const val NFC_TYPE_UNDER_SCREEN = "under_screen"

        /** External NFC module type identifier (USB-connected reader). */
        private const val NFC_TYPE_EXTERNAL = "external"
    }
}
