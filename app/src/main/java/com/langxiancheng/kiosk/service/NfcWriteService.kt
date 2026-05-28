package com.langxiancheng.kiosk.service

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for writing NDEF records to NFC tags.
 * Handles the low-level NFC communication for writing URLs to NFC tags.
 *
 * The URL format follows the specification:
 * https://cafe.langxiancheng.com/result?d={drinkId}&s={scoreHash}&t={timestamp}
 */
@Singleton
class NfcWriteService @Inject constructor() {

    /**
     * Writes a URL as an NDEF record to the given NFC tag.
     * Uses try-finally to ensure the NDEF connection is always closed,
     * preventing resource leaks on exceptions.
     *
     * @param url The URL to write to the tag
     * @param tag The NFC tag to write to
     * @return true if the write was successful, false otherwise
     */
    suspend fun writeNdefToTag(url: String, tag: Tag): Boolean {
        return withContext(Dispatchers.IO) {
            val ndef = Ndef.get(tag) ?: return@withContext false

            try {
                ndef.connect()
                val ndefMessage = createNdefMessage(url)

                if (!ndef.isWritable) {
                    return@withContext false
                }

                if (ndef.maxSize < ndefMessage.byteArrayLength) {
                    return@withContext false
                }

                ndef.writeNdefMessage(ndefMessage)
                true
            } catch (e: IOException) {
                false
            } catch (e: FormatException) {
                false
            } catch (e: Exception) {
                false
            } finally {
                // Always close the NDEF connection to prevent resource leaks
                try {
                    ndef.close()
                } catch (_: Exception) {
                    // Ignore close exceptions — the connection may already be closed
                }
            }
        }
    }

    /**
     * Writes a URL to an NFC tag using the NfcAdapter.
     * This is a simplified version for when no specific tag is available yet.
     *
     * @param url The URL to write
     * @return true if the write was successful, false otherwise
     */
    suspend fun writeNdefUrl(url: String): Boolean {
        // In a real kiosk deployment, this would use the foreground dispatch
        // to get the actual tag when the user taps it.
        // For now, we simulate a successful write for UI testing purposes.
        return withContext(Dispatchers.IO) {
            try {
                // Simulate NFC write delay
                kotlinx.coroutines.delay(1500L)
                // In production, this would write to an actual NFC tag
                // using NfcAdapter.enableForegroundDispatch()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Creates an NDEF message containing a URI record.
     *
     * @param url The URL to encode
     * @return NdefMessage containing the URI record
     */
    private fun createNdefMessage(url: String): NdefMessage {
        val uriRecord = NdefRecord.createUri(url)
        return NdefMessage(uriRecord)
    }

    /**
     * Creates an NDEF message with both a URI and a text record.
     * Used for tags that need a human-readable label.
     *
     * @param url The URL to encode
     * @param title The title/label text
     * @return NdefMessage containing both records
     */
    fun createNdefMessageWithTitle(url: String, title: String): NdefMessage {
        val uriRecord = NdefRecord.createUri(url)
        val textRecord = createTextRecord(title, "zh")
        return NdefMessage(arrayOf(uriRecord, textRecord))
    }

    /**
     * Creates a text NDEF record.
     *
     * @param text The text content
     * @param languageCode ISO 639-1 language code
     * @return NdefRecord with text MIME type
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

        /** Base URL for NFC result links. */
        const val BASE_URL = "https://cafe.langxiancheng.com/result"
    }
}
