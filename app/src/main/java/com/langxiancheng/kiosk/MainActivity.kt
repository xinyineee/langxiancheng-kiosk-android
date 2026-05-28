package com.langxiancheng.kiosk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.langxiancheng.kiosk.ui.navigation.KioskNavGraph
import com.langxiancheng.kiosk.ui.theme.LangXianChengKioskTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity entry point for the Kiosk application.
 * Hosts the Compose navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
}
