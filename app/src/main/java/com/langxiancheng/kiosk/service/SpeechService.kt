package com.langxiancheng.kiosk.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Speech recognition service for voice-driven quiz interaction.
 *
 * Maps spoken keywords to quiz option labels:
 * - "浪" (lang) → Option A
 * - "险" (xian) → Option B
 * - "成" (cheng) → Option C
 * - "橙" (cheng/orange) → Option D
 *
 * Gracefully degrades if speech recognition is unavailable.
 */
@Singleton
class SpeechService @Inject constructor() {

    private val _recognizedKeyword = MutableStateFlow<String?>(null)
    val recognizedKeyword: StateFlow<String?> = _recognizedKeyword.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Initializes the speech recognizer.
     * Should be called when the hosting Activity is created.
     *
     * @param context Android context (usually the Activity)
     */
    fun initialize(context: Context) {
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        _isAvailable.value = available

        if (available) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        // Speech input has started
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Audio level changed
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Audio buffer received
                    }

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> {
                                // No speech matched — ignore and continue
                            }
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                // No speech detected — restart listening
                                restartListening()
                            }
                            else -> {
                                // Other errors — don't crash, graceful degradation
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            processSpeechInput(matches[0])
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            processSpeechInput(matches[0])
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {
                        // Reserved for future events
                    }
                })
            }
        }
    }

    /**
     * Starts listening for speech input.
     */
    fun startListening() {
        if (!_isAvailable.value) return
        val recognizer = speechRecognizer ?: return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        recognizer.startListening(intent)
    }

    /**
     * Stops listening for speech input.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    /**
     * Processes the recognized speech text and maps keywords to option labels.
     *
     * @param speechText The recognized text from the speech recognizer
     */
    private fun processSpeechInput(speechText: String) {
        val normalizedText = speechText.lowercase()

        val mappedOption = when {
            normalizedText.contains("浪") -> "A"
            normalizedText.contains("险") -> "B"
            normalizedText.contains("成") -> "C"
            normalizedText.contains("橙") -> "D"
            else -> null
        }

        if (mappedOption != null) {
            _recognizedKeyword.value = mappedOption
        }
    }

    /**
     * Restarts listening after a brief delay using the main handler.
     */
    private fun restartListening() {
        if (_isAvailable.value) {
            mainHandler.postDelayed({ startListening() }, 500L)
        }
    }

    /**
     * Clears the current recognized keyword.
     */
    fun clearKeyword() {
        _recognizedKeyword.value = null
    }

    /**
     * Releases the speech recognizer resources.
     * Should be called when the hosting Activity is destroyed.
     */
    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
        _isAvailable.value = false
    }

    companion object {
        private const val TAG = "SpeechService"

        /** Keyword-to-option mapping for the quiz. */
        val KEYWORD_MAP = mapOf(
            "浪" to "A",
            "险" to "B",
            "成" to "C",
            "橙" to "D"
        )
    }
}
