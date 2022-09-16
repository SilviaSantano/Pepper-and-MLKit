package de.inovex.pepper.intelligence.mlkit.system.di

import com.google.mlkit.nl.translate.TranslateLanguage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.inovex.pepper.intelligence.mlkit.ui.reading.ImageTextAnalyzer
import de.inovex.pepper.intelligence.mlkit.ui.seeing.ImageAnalyzer
import de.inovex.pepper.intelligence.mlkit.ui.translating.TextTranslator
import de.inovex.pepper.intelligence.mlkit.utils.Language
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AnalyzerModule {

    @Provides
    @Singleton
    fun provideImageTextAnalyzer() = ImageTextAnalyzer()

    @Provides
    @Singleton
    fun provideImageAnalyzer() = ImageAnalyzer()

    @Provides
    @Singleton
    fun provideTextTranslator() = TextTranslator(
        mapOf(
            Language.ENGLISH to TranslateLanguage.ENGLISH,
            Language.GERMAN to TranslateLanguage.GERMAN,
            Language.SPANISH to TranslateLanguage.SPANISH
        )
    )
}
