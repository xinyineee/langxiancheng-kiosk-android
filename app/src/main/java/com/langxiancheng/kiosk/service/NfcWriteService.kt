package com.langxiancheng.kiosk.service

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log
import com.langxiancheng.kiosk.ui.component.NfcWriteState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for writing NDEF records to NFC tags.
 *
 * Architecture:
 * - ResultViewModel calls [prepareWrite] to set URL and change state to WRITING
 * - MainActivity receives NFC tag via foreground dispatch and calls [writePendingTag]
 * - The [writeState] StateFlow is observed by ResultViewModel for UI updates
 * - ResultViewModel handles TTS feedback and state reset timing
 *
 * The URL format:
 * https://cafe.langxiancheng.com/result?d={drinkId}&s={scoreHash}&t={timestamp}
 */
@Singleton
class NfcWriteService @Inject constructor() {

    /** URL pending to be written to the next discovered NFC tag. */
    private var pendingUrl: String? = null

    /** Whether we are ready and waiting for a tag tap. */
    var isWaitingForTap: Boolean = false
        private set

    private val _writeState = MutableStateFlow(NfcWriteState.IDLE)
    /** Observable NFC write state for UI components. */
    val writeState: StateFlow<NfcWriteState> = _writeState.asStateFlow()

    /**
     * Prepares a URL for writing to the next NFC tag.
     *
     * @param url The URL to write to the next NFC tag
     */
    fun prepareWrite(url: String) {
        pendingUrl = url
        isWaitingForTap = true
        _writeState.value = NfcWriteState.WRITING
        Log.d(TAG, "NFC write prepared. URL: $url")
    }

    /**
     * Writes the pending URL to the given NFC tag.
     * Called by MainActivity when a tag is discovered via foreground dispatch.
     *
     * @param tag The NFC tag discovered by the system
     * @return true if the write was successful, false otherwise
     */
    suspend fun writePendingTag(tag: Tag): Boolean {
        val url = pendingUrl ?: run {
            Log.w(TAG, "No pending URL to write")
            _writeState.value = NfcWriteState.FAILURE
            return false
        }

        val result = writeNdefToTagInternal(url, tag)

        // Clear pending state after write attempt
        pendingUrl = null
        isWaitingForTap = false
        _writeState.value = if (result) NfcWriteState.SUCCESS else NfcWriteState.FAILURE

        return result
    }

    /**
     * Cancels the pending NFC write operation.
     */
    fun cancelPendingWrite() {
        pendingUrl = null
        isWaitingForTap = false
        _writeState.value = NfcWriteState.IDLE
        Log.d(TAG, "NFC write cancelled")
    }

    /**
     * Resets write state to IDLE. Called by ResultViewModel after UI feedback.
     */
    fun resetWriteState() {
        _writeState.value = NfcWriteState.IDLE
    }

    /**
     * Writes a URL as an NDEF record to the given NFC tag.
     */
    private suspend fun writeNdefToTagInternal(url: String, tag: Tag): Boolean {
        return withContext(Dispatchers.IO) {
            val ndef = Ndef.get(tag) ?: run {
                Log.w(TAG, "Tag does not support NDEF")
                return@withContext false
            }

            try {
                ndef.connect()
                val ndefMessage = createNdefMessage(url)

                if (!ndef.isWritable) {
                    Log.w(TAG, "Tag is not writable")
                    return@withContext false
                }

                if (ndef.maxSize < ndefMessage.byteArrayLength) {
                    Log.w(TAG, "Tag capacity too small: ${ndef.maxSize} < ${ndefMessage.byteArrayLength}")
                    return@withContext false
                }

                ndef.writeNdefMessage(ndefMessage)
                Log.d(TAG, "NFC write successful: $url")
                true
            } catch (e: IOException) {
                Log.e(TAG, "NFC IO error", e)
                false
            } catch (e: FormatException) {
                Log.e(TAG, "NFC format error", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "NFC write error", e)
                false
            } finally {
                try {
                    ndef.close()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Creates an NDEF message containing a URI record.
     */
    private fun createNdefMessage(url: String): NdefMessage {
        val uriRecord = NdefRecord.createUri(url)
        return NdefMessage(uriRecord)
    }

    /**
     * Creates an NDEF message with both a URI and a text record.
     */
    fun createNdefMessageWithTitle(url: String, title: String): NdefMessage {
        val uriRecord = NdefRecord.createUri(url)
        val textRecord = createTextRecord(title, "zh")
        return NdefMessage(arrayOf(uriRecord, textRecord))
    }

    /**
     * Creates a text NDEF record.
     */
    private fun createTextRecord(text: String, languageCode: String): NdefRecord {
        val languageBytes = languageCode.toByteArray(StandardCharsets.US_ASCII)
        val textBytes = text.toByteArray(StandardCharsets.UTF_8)
        val payload = ByteArray(1 + languageBytes.size + textBytes.size)

        payload[0] = (languageBytes.size.toByte())
        System.arraycopy(languageBytes, 0, payload, 1, languageBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + languageBytes.size, textBytes.size)

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }

    companion object {
        private const val TAG = "NfcWriteService"
        const val BASE_URL = "https://cafe.langxiancheng.com/result"
    }
}
