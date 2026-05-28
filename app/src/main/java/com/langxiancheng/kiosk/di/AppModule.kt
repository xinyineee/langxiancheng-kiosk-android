package com.langxiancheng.kiosk.di

import com.langxiancheng.kiosk.service.NfcWriteService
import com.langxiancheng.kiosk.service.SpeechService
import com.langxiancheng.kiosk.service.TimerService
import com.langxiancheng.kiosk.service.TtsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module providing service instances.
 *
 * Note: TestDataRepository and TestEngine already have @Singleton + @Inject constructor,
 * so Hilt can create them automatically without explicit @Provides methods.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the NfcWriteService as a singleton.
     */
    @Provides
    @Singleton
    fun provideNfcWriteService(): NfcWriteService {
        return NfcWriteService()
    }

    /**
     * Provides the TimerService as a singleton.
     */
    @Provides
    @Singleton
    fun provideTimerService(): TimerService {
        return TimerService()
    }

    /**
     * Provides the SpeechService as a singleton.
     * Note: Must call initialize(context) before use.
     */
    @Provides
    @Singleton
    fun provideSpeechService(): SpeechService {
        return SpeechService()
    }

    /**
     * Provides the TtsService as a singleton.
     * Note: Must call initialize(context) before use.
     */
    @Provides
    @Singleton
    fun provideTtsService(): TtsService {
        return TtsService()
    }
}
