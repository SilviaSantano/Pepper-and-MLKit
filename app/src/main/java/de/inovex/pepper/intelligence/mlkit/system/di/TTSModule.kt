package de.inovex.pepper.intelligence.mlkit.system.di

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class TTSModule {

    @Provides
    fun provideTTS(@ApplicationContext context: Context) = TextToSpeech(context) {}
}
