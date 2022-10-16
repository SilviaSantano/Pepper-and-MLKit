package de.inovex.pepper.intelligence.mlkit.ui.translating

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.inovex.pepper.intelligence.mlkit.R
import de.inovex.pepper.intelligence.mlkit.databinding.FragmentTranslatingBinding
import de.inovex.pepper.intelligence.mlkit.ui.main.MainViewModel
import de.inovex.pepper.intelligence.mlkit.utils.Language
import de.inovex.pepper.intelligence.mlkit.utils.languageMap
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TranslatingFragment : Fragment() {

    private val viewModel: TranslatingViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentTranslatingBinding

    @Inject
    lateinit var tts: TextToSpeech

    private lateinit var translatedText: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTranslatingBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewmodel = viewModel
            }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // Button Listeners
        viewModel.uiEvents.observe(viewLifecycleOwner) {
            when (it) {
                TranslatingViewModel.UiEvent.ExplainRules -> explainTranslatingRules()
            }
        }

        // Reset results text
        showTranslationResults("")

        // Explain Demo Rules
        explainTranslatingRules()
    }

    override fun onDestroy() {
        // Stop the TTS
        tts.apply {
            this.stop()
            this.shutdown()
        }
        super.onDestroy()
    }

    private fun explainTranslatingRules() {
        mainViewModel.goToQiChatBookmark(getString(R.string.translatingRulesBookmark))
    }

    fun translate() {
        val textToTranslate = mainViewModel.getQiChatVariable(getString(R.string.textToTranslate))
        val toLanguage = languageMap[
            mainViewModel.getQiChatVariable(getString(R.string.toLanguage)).lowercase()
        ]

        Timber.d("Asked to translate '$textToTranslate' to $toLanguage")

        if (toLanguage != Language.OTHER) {
            mainViewModel.translate(
                mainViewModel.language,
                toLanguage!!,
                textToTranslate
            ).addOnSuccessListener { text ->
                Timber.d("The translation is '$text'")
                goToTranslatedBookmark(text)
                showTranslationResults(text)
            }
        } else {
            goToTranslationNotPossibleBookmark()
        }
    }

    private fun showTranslationResults(text: String?) {
        try {
            requireActivity().runOnUiThread {
                binding.translatingResultsTextView.text = text
            }
        } catch (e: Exception) {
            Timber.w("Could not show recognition results due to $e")
        }
    }

    private fun goToTranslatedBookmark(text: String) {
        translatedText = text

        mainViewModel.setQiChatVariable(getString(R.string.pronounceTranslationBookmark), text)
        mainViewModel.goToQiChatBookmark(getString(R.string.translatedBookmark))
    }

    fun pronounceTranslation() {
        val toLanguage = languageMap[
            mainViewModel.getQiChatVariable(getString(R.string.toLanguage)).lowercase()
        ]

        tts.language =
            when (toLanguage) {
                Language.ENGLISH -> Locale.ENGLISH
                Language.GERMAN -> Locale.GERMAN
                else -> Locale("es", "ES")
            }
        tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun goToTranslationNotPossibleBookmark() {
        mainViewModel.goToQiChatBookmark(getString(R.string.TranslationNotPossibleBookmark))
        showTranslationResults("")
    }
}
