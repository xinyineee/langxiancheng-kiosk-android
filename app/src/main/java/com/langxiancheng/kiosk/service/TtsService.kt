package com.langxiancheng.kiosk.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Text-to-Speech service for voice interaction throughout the app.
 *
 * Supports:
 * - Welcome greeting
 * - Question + options reading
 * - Selection feedback
 * - Result announcement (drink name + tagline + heart copy)
 * - NFC feedback
 *
 * Gracefully degrades if TTS is unavailable.
 */
@Singleton
class TtsService @Inject constructor() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var onCompleteCallback: (() -> Unit)? = null

    /**
     * Initializes the TTS engine.
     * Must be called from the main thread with a valid context.
     */
    fun initialize(context: Context) {
        if (textToSpeech != null) return // Already initialized

        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val tts = textToSpeech ?: return@TextToSpeech
                val result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
                _isReady.value = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED

                tts.setPitch(1.05f)
                tts.setSpeechRate(1.05f)

                // Set up utterance listener for completion callbacks
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        onCompleteCallback?.invoke()
                        onCompleteCallback = null
                    }
                    override fun onError(utteranceId: String?) {}
                })
            } else {
                _isReady.value = false
            }
        }
    }

    /**
     * Speaks the given text aloud.
     */
    fun speak(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        utteranceId: String = "utterance"
    ) {
        if (!_isReady.value) return
        textToSpeech?.speak(text, queueMode, null, utteranceId)
    }

    /**
     * Speaks text with a completion callback.
     */
    fun speakWithCallback(
        text: String,
        onComplete: () -> Unit,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        if (!_isReady.value) {
            onComplete()
            return
        }
        onCompleteCallback = onComplete
        textToSpeech?.speak(text, queueMode, null, "callback_utterance")
    }

    /**
     * Idle screen welcome greeting.
     */
    fun speakWelcome() {
        speak("欢迎来到浪险橙咖啡，触摸屏幕开始测试", TextToSpeech.QUEUE_FLUSH, "welcome")
    }

    /**
     * Welcome screen intro.
     */
    fun speakWelcomeIntro() {
        speak("5道趣味题，30秒测出你的专属特调。准备好了吗？", TextToSpeech.QUEUE_FLUSH, "welcome_intro")
    }

    /**
     * Speaks the question text and all options.
     */
    fun speakQuestion(questionText: String, options: List<String>) {
        if (!_isReady.value) return
        val sb = StringBuilder()
        sb.append("第")
        sb.append(questionText)
        sb.append("。选项：")
        options.forEachIndexed { index, option ->
            sb.append("${index + 1}、$option；")
        }
        speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, "question")
    }

    /**
     * Speaks a single option as user considers it.
     */
    fun speakOption(label: String, text: String) {
        speak("$label：$text", TextToSpeech.QUEUE_ADD, "option")
    }

    /**
     * Feedback when user selects an option.
     */
    fun speakSelectionFeedback(label: String) {
        speak("已选择$label", TextToSpeech.QUEUE_FLUSH, "selection")
    }

    /**
     * Announces the test result with drink name, tagline, and heart copy.
     */
    fun speakResult(drinkName: String, tagline: String, heartCopy: String) {
        if (!_isReady.value) return
        val text = "你的专属特调是：$drinkName。$tagline。$heartCopy"
        speak(text, TextToSpeech.QUEUE_FLUSH, "result")
    }

    /**
     * NFC write success feedback.
     */
    fun speakNfcSuccess() {
        speak("NFC标签写入成功，请将手机靠近标签", TextToSpeech.QUEUE_FLUSH, "nfc_success")
    }

    /**
     * NFC write failure feedback.
     */
    fun speakNfcFailed() {
        speak("NFC写入失败，请重试", TextToSpeech.QUEUE_FLUSH, "nfc_failed")
    }

    /**
     * Countdown warning when time is running out.
     */
    fun speakTimeWarning() {
        speak("时间快到了", TextToSpeech.QUEUE_FLUSH, "time_warning")
    }

    /**
     * Stops any current TTS output.
     */
    fun stop() {
        textToSpeech?.stop()
        onCompleteCallback = null
    }

    /**
     * Releases TTS resources.
     */
    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isReady.value = false
        onCompleteCallback = null
    }

    companion object {
        private const val TAG = "TtsService"
    }
}
