package com.langxiancheng.kiosk

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.langxiancheng.kiosk.service.NfcWriteService
import com.langxiancheng.kiosk.service.SpeechService
import com.langxiancheng.kiosk.service.SunmiNfcService
import com.langxiancheng.kiosk.service.TtsService
import com.langxiancheng.kiosk.ui.navigation.KioskNavGraph
import com.langxiancheng.kiosk.ui.theme.LangXianChengKioskTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single Activity entry point for the Kiosk application.
 * Hosts the Compose navigation graph.
 *
 * NFC Integration:
 * - Initializes Sunmi SDK for FLEX 3 under-screen NFC module control
 * - Enables standard Android NFC foreground dispatch for tag discovery
 * - Handles NFC tag discovery in onNewIntent() and delegates to NfcWriteService
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var ttsService: TtsService

    @Inject
    lateinit var speechService: SpeechService

    @Inject
    lateinit var nfcWriteService: NfcWriteService

    @Inject
    lateinit var sunmiNfcService: SunmiNfcService

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize NFC
        initNfc()

        // Initialize voice services
        ttsService.initialize(this)
        speechService.initialize(this)

        // Initialize Sunmi NFC SDK for FLEX 3 under-screen NFC
        initSunmiNfc()

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: action=${intent?.action}")

        if (intent != null) {
            val tag = when (intent.action) {
                NfcAdapter.ACTION_TAG_DISCOVERED,
                NfcAdapter.ACTION_NDEF_DISCOVERED,
                NfcAdapter.ACTION_TECH_DISCOVERED -> intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                else -> null
            }

            if (tag != null) {
                Log.d(TAG, "NFC tag discovered: ${tag.techList.joinToString()}")
                handleNfcTagDiscovered(tag)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsService.release()
        speechService.release()
        sunmiNfcService.release()
        nfcWriteService.cancelPendingWrite()
    }

    private fun initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Log.w(TAG, "Device does not support NFC")
        } else if (!nfcAdapter!!.isEnabled) {
            Log.w(TAG, "NFC is disabled on this device")
            Toast.makeText(this, "请开启NFC功能", Toast.LENGTH_LONG).show()
        } else {
            Log.d(TAG, "NFC adapter initialized")
        }
    }

    private fun initSunmiNfc() {
        sunmiNfcService.initialize { success ->
            if (success) {
                Log.d(TAG, "Sunmi NFC SDK initialized. Under-screen NFC ready.")
                sunmiNfcService.setWatermarkAlpha(80)
            } else {
                Log.w(TAG, "Sunmi NFC SDK init failed. Will use standard NFC only.")
            }
        }
    }

    private fun enableNfcForegroundDispatch() {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        try {
            val intent = Intent(this, javaClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent,
                android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val intentFilters = arrayOf(
                android.content.IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
            )

            val techLists = arrayOf(
                arrayOf(android.nfc.tech.Ndef::class.java.name),
                arrayOf(android.nfc.tech.NfcA::class.java.name),
                arrayOf(android.nfc.tech.NfcB::class.java.name),
                arrayOf(android.nfc.tech.NfcF::class.java.name),
                arrayOf(android.nfc.tech.NfcV::class.java.name)
            )

            adapter.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists)
            Log.d(TAG, "NFC foreground dispatch enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable NFC foreground dispatch", e)
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            nfcAdapter?.disableForegroundDispatch(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable NFC foreground dispatch", e)
        }
    }

    private fun handleNfcTagDiscovered(tag: Tag) {
        if (nfcWriteService.isWaitingForTap) {
            lifecycleScope.launch {
                val success = nfcWriteService.writePendingTag(tag)
                if (success) {
                    ttsService.speakNfcSuccess()
                    Toast.makeText(this@MainActivity, "NFC写入成功！", Toast.LENGTH_SHORT).show()
                } else {
                    ttsService.speakNfcFailed()
                    Toast.makeText(this@MainActivity, "NFC写入失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.d(TAG, "NFC tag discovered but no pending write operation")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
