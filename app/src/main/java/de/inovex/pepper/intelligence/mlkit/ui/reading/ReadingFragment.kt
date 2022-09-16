package de.inovex.pepper.intelligence.mlkit.ui.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.inovex.pepper.intelligence.R
import de.inovex.pepper.intelligence.databinding.FragmentReadingBinding
import de.inovex.pepper.intelligence.mlkit.ui.main.MainViewModel
import de.inovex.pepper.intelligence.mlkit.utils.toggleVisibility
import timber.log.Timber

@AndroidEntryPoint
class ReadingFragment : Fragment() {

    private val viewModel: ReadingViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentReadingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadingBinding.inflate(inflater, container, false)
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
                ReadingViewModel.UiEvent.ExplainRules -> explainReadingRules()
                ReadingViewModel.UiEvent.TogglePreview -> togglePreview()
            }
        }

        // Clear results view
        updateRecognizedText("")

        // This is triggered when a new image has been taken with the head camera
        viewModel.getLastImage().observe(viewLifecycleOwner) { image ->

            Timber.d("Got new image")
            // Show the preview on the tablet
            try {
                requireActivity().runOnUiThread {
                    binding.readingImageView.setImageBitmap(image)
                }
            } catch (e: Exception) {
                Timber.w("Could not show recognition results due to $e")
            }

            // Analyze the image to extract text
            viewModel.extractTextFromImage(image)
        }

        // Get the text from the last image, update UI and chat
        viewModel.getReadText().observe(viewLifecycleOwner) { text ->

            if (text != "Nothing recognized") {
                Timber.i("Found this text in the image: $text}")
                updateRecognizedText(text)
            } else {
                updateRecognizedText("")
            }

            // Take the next image
            this.viewModel.takeImage(mainViewModel.qiContext)
        }

        // Explain Demo Rules
        explainReadingRules()

        // Take the first image to start
        this.viewModel.takeImage(mainViewModel.qiContext)
    }

    private fun explainReadingRules() {
        mainViewModel.goToQiChatBookmark(getString(R.string.readingRulesBookmark))
    }

    private fun togglePreview() {
        binding.readingImageView.toggleVisibility()
    }

    private fun updateRecognizedText(text: String) {
        try {
            requireActivity().runOnUiThread {
                binding.readingResultsTextView.text = text
            }
        } catch (e: Exception) {
            Timber.w("Could not show recognition results due to $e")
        }

        // Save the results in a variable for them to available in the chat
        mainViewModel.setQiChatVariable(getString(R.string.recognizedText), text)
    }
}
