package de.inovex.pepper.intelligence.mlkit.ui.drawing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.inovex.pepper.intelligence.mlkit.R
import de.inovex.pepper.intelligence.mlkit.databinding.FragmentDrawingBinding
import de.inovex.pepper.intelligence.mlkit.ui.main.MainViewModel
import de.inovex.pepper.intelligence.mlkit.utils.Language
import de.inovex.pepper.intelligence.mlkit.utils.Mode

@AndroidEntryPoint
class DrawingFragment :
    Fragment(),
    StrokeManager.ContentChangedListener {

    private val viewModel: DrawingViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentDrawingBinding
    private val strokeManager = StrokeManager()
    private var mode = Mode.GERMAN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawingBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewmodel = viewModel
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init Stroke Manager
        binding.drawingDrawingView.setStrokeManager(strokeManager)
        strokeManager.setContentChangedListener(this)
        strokeManager.setClearCurrentInkAfterRecognition(true)
        strokeManager.setTriggerRecognitionAfterInput(false)
        strokeManager.reset()

        // Download MLKit Ink Recognition models and select active model
        strokeManager.downloadModels()
        mode = selectActiveMode()
        strokeManager.setActiveModel(mode.string)

        // Model changed listener
        binding.drawingModelSwitch.setOnCheckedChangeListener { _, _ ->
            mode = selectActiveMode()
            strokeManager.setActiveModel(mode.string)

            mainViewModel.setQiChatVariable(
                getString(R.string.GameMode),
                if (mode == Mode.DRAWING) getString(R.string.drawingMode)
                else getString(
                    R.string.textMode
                )
            )
            mainViewModel.goToQiChatBookmark(getString(R.string.changedModeBookmark))
        }

        // Button Listeners
        viewModel.uiEvents.observe(viewLifecycleOwner) {
            when (it) {
                DrawingViewModel.UiEvent.ExplainRules -> explainDrawingRules()
                DrawingViewModel.UiEvent.Clear -> clear()
                DrawingViewModel.UiEvent.RecognizeDrawing -> recognizeDrawing()
            }
        }
        explainDrawingRules()
    }

    private fun explainDrawingRules() {
        mainViewModel.goToQiChatBookmark(getString(R.string.drawingRulesBookmark))
    }

    override fun onDrawingContentChanged() {
        if (strokeManager.getContent().isNotEmpty()) {
            translateAndSayResult(strokeManager.getContent()[0].text ?: "")
        } else {
            goToNotRecognizedDrawingBookmark()
        }
    }

    private fun translateAndSayResult(text: String) {
        // Translate from english to the robot language if necessary and then go to the bookmark
        if (mode != Mode.DRAWING || mainViewModel.language == Language.ENGLISH) {
            goToRecognizedDrawingBookmark(text)
        } else {
            mainViewModel.translate(Language.ENGLISH, mainViewModel.language, text)
                .addOnSuccessListener { translated ->
                    goToRecognizedDrawingBookmark(translated)
                }
        }
    }

    private fun goToRecognizedDrawingBookmark(text: String) {
        if (text == getString(R.string.inovex)) {
            recognizedTextInovex()
        } else {
            mainViewModel.setQiChatVariable(getString(R.string.recognizedDrawing), text)
            mainViewModel.goToQiChatBookmark(getString(R.string.recognizedDrawingBookmark))
        }
    }

    private fun recognizedTextInovex() {
        mainViewModel.performHappyPepperAnimation(requireContext())
        mainViewModel.goToQiChatBookmark(getString(R.string.recognizedInovexBookmark))
    }

    private fun goToNotRecognizedDrawingBookmark() {
        mainViewModel.goToQiChatBookmark(getString(R.string.notRecognizeDrawingBookmark))
    }

    private fun selectActiveMode(): Mode {
        return if (!binding.drawingModelSwitch.isChecked) {
            Mode.DRAWING
        } else when (mainViewModel.language) {
            Language.ENGLISH -> Mode.ENGLISH
            Language.SPANISH -> Mode.SPANISH
            else -> Mode.GERMAN
        }
    }

    fun recognizeDrawing() {
        strokeManager.recognize()
    }

    fun clear() {
        strokeManager.reset()
        binding.drawingDrawingView.clear()
    }
}
