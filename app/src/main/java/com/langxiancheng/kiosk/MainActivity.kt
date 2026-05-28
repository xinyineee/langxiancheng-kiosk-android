package com.langxiancheng.kiosk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.langxiancheng.kiosk.service.SpeechService
import com.langxiancheng.kiosk.service.TtsService
import com.langxiancheng.kiosk.ui.navigation.KioskNavGraph
import com.langxiancheng.kiosk.ui.theme.LangXianChengKioskTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single Activity entry point for the Kiosk application.
 * Hosts the Compose navigation graph.
 * Initializes TTS and speech recognition services.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var ttsService: TtsService

    @Inject
    lateinit var speechService: SpeechService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize voice services
        ttsService.initialize(this)
        speechService.initialize(this)

        setContent {
            LangXianChengKioskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    KioskNavGraph()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsService.release()
        speechService.release()
    }
}
