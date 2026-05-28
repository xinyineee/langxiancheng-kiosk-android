package com.langxiancheng.kiosk

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for LangXianCheng Kiosk.
 * Initializes Hilt dependency injection.
 */
@HiltAndroidApp
class KioskApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application-level initialization
    }
}
