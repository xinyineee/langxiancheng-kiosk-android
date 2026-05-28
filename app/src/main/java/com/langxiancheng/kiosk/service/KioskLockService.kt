package com.langxiancheng.kiosk.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Kiosk mode lock service that keeps the device locked to the Kiosk application.
 *
 * Features:
 * - Monitors that the kiosk app stays in the foreground
 * - Restores the app if another activity comes to the front
 * - Works with Android's Lock Task Mode for dedicated device deployments
 *
 * In a production deployment, this would be combined with:
 * 1. DevicePolicyManager to enable lock task mode
 * 2. Device owner setup via ADB or EMM
 * 3. startLockTask() in the Activity
 */
class KioskLockService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var watchdogJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startKioskMode()
        startWatchdog()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        watchdogJob?.cancel()
    }

    /**
     * Initializes kiosk mode settings.
     * For development, this ensures the service stays running.
     */
    private fun startKioskMode() {
        // Production would use DevicePolicyManager + lock task mode
    }

    /**
     * Starts a watchdog timer that periodically checks
     * that the kiosk app is in the foreground.
     */
    private fun startWatchdog() {
        watchdogJob = serviceScope.launch {
            while (true) {
                delay(WATCHDOG_INTERVAL_MS)
                checkKioskState()
            }
        }
    }

    /**
     * Checks and restores kiosk state if another app has come to the foreground.
     */
    private fun checkKioskState() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val runningTasks = activityManager?.appTasks

        runningTasks?.forEach { taskInfo ->
            val baseActivity = taskInfo.taskInfo.baseActivity?.className
            if (baseActivity != null && !baseActivity.startsWith("com.langxiancheng.kiosk")) {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                launchIntent?.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
                startActivity(launchIntent)
            }
        }
    }

    companion object {
        private const val TAG = "KioskLockService"
        private const val WATCHDOG_INTERVAL_MS = 3000L
    }
}
