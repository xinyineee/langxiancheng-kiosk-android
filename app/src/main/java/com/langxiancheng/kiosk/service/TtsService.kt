package com.langxiancheng.kiosk.service

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Text-to-Speech service for reading out quiz content and results.
 *
 * Used for:
 * - Reading question text aloud for accessibility
 * - Announcing the test result
 * - Providing audio feedback for NFC operations
 *
 * Gracefully degrades if TTS is unavailable.
 */
@Singleton
class TtsService @Inject constructor() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var textToSpeech: TextToSpeech? = null

    /**
     * Initializes the TTS engine.
     * Must be called from the main thread with a valid context.
     *
     * @param context Android context
     */
    fun initialize(context: Context) {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val tts = textToSpeech ?: return@TextToSpeech
                val result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
                _isReady.value = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED

                // Configure TTS parameters
                tts.setPitch(1.0f)
                tts.setSpeechRate(1.0f)
            } else {
                _isReady.value = false
            }
        }
    }

    /**
     * Speaks the given text aloud.
     *
     * @param text The text to speak
     * @param queueMode The queue mode (QUEUE_ADD or QUEUE_FLUSH)
     * @param utteranceId Optional utterance ID for completion callback
     */
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        utteranceId: String = ""
    ) {
        if (!_isReady.value) return
        textToSpeech?.speak(text, queueMode, null, utteranceId)
    }

    /**
     * Speaks the question text with a brief pause before options.
     *
     * @param questionText The question text to read
     */
    fun speakQuestion(questionText: String) {
        if (!_isReady.value) return
        textToSpeech?.speak(questionText, TextToSpeech.QUEUE_FLUSH, null, "question")
    }

    /**
     * Speaks the test result drink name and tagline.
     *
     * @param drinkName The name of the recommended drink
     * @param tagline The drink's tagline
     */
    fun speakResult(drinkName: String, tagline: String) {
        if (!_isReady.value) return
        val resultText = "你的专属特调是$drinkName。$tagline"
        textToSpeech?.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, "result")
    }

    /**
     * Stops any current TTS output.
     */
    fun stop() {
        textToSpeech?.stop()
    }

    /**
     * Releases TTS resources.
     * Must be called when the hosting Activity is destroyed.
     */
    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isReady.value = false
    }

    companion object {
        private const val TAG = "TtsService"
    }
}
