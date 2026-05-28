package com.langxiancheng.kiosk.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.langxiancheng.kiosk.service.TtsService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Shared ViewModel providing TTS service access to screens without their own ViewModel.
 * Used by IdleScreen and WelcomeScreen for voice greetings.
 */
@HiltViewModel
class KioskSharedViewModel @Inject constructor(
    val ttsService: TtsService
) : ViewModel()
