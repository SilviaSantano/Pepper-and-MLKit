package de.inovex.pepper.intelligence.mlkit.ui.translating

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import de.inovex.pepper.intelligence.mlkit.utils.Language

class TextTranslator(
    private var translatingLanguageMap: Map<Language, String>
) {
    fun translate(source: Language, target: Language, text: String): Task<String> {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(translatingLanguageMap[source]!!)
            .setTargetLanguage(translatingLanguageMap[target]!!)
            .build()
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)

        return translator.translate(text)
    }
}
