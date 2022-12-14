package de.inovex.pepper.intelligence.mlkit.ui.seeing

import android.graphics.Canvas
import android.os.Bundle
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toRectF
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.mlkit.common.model.LocalModel
import dagger.hilt.android.AndroidEntryPoint
import de.inovex.pepper.intelligence.mlkit.R
import de.inovex.pepper.intelligence.mlkit.databinding.FragmentSeeingBinding
import de.inovex.pepper.intelligence.mlkit.ui.main.MainViewModel
import de.inovex.pepper.intelligence.mlkit.utils.HEIGHT
import de.inovex.pepper.intelligence.mlkit.utils.Language
import de.inovex.pepper.intelligence.mlkit.utils.roundConfidence
import de.inovex.pepper.intelligence.mlkit.utils.toggleVisibility
import timber.log.Timber

@AndroidEntryPoint
class SeeingFragment : androidx.fragment.app.Fragment() {

    private val viewModel: SeeingViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentSeeingBinding
    var items: MutableList<Recognition> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeeingBinding.inflate(inflater, container, false)
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
                SeeingViewModel.UiEvent.ExplainRules -> explainSeeingRules()
                SeeingViewModel.UiEvent.TogglePreview -> togglePreview()
            }
        }

        // Reset results text
        updateRecognizedTextChatVariable("")

        // Custom OD Model
        val localModel = LocalModel.Builder()
            .setAssetFilePath("lite-model_object_detection_mobile_object_labeler_v1_1.tflite")
            .build()

        // This is triggered when a new image has been taken with the head camera
        viewModel.getLastImage().observe(viewLifecycleOwner) { image ->

            Timber.d("Got new image")
            // Show the preview on the tablet
            try {
                requireActivity().runOnUiThread {
                    binding.seeingPreviewView.setImageBitmap(image)
                }
            } catch (e: Exception) {
                Timber.w("Could not show recognition results due to $e")
            }

            // Analyze the contents in the image
            viewModel.analyzeImageWithMLKitObjectDetector(localModel, image)
        }

        // This gets triggered when the results of the analyzer are ready
        viewModel.getMLKitRecognitionObjects().observe(viewLifecycleOwner) { objects ->
            // Prepare the chat variable including all results, translating if necessary
            var labelsText = ""
            objects.forEach {
                if (it.labels.size > 0 && it.labels[0].confidence > MIN_CONFIDENCE) {
                    if (labelsText.isNotBlank()) labelsText += ", "
                    labelsText += "  ${it.labels[0].text}"
                }
            }
            when (mainViewModel.language) {
                Language.ENGLISH -> updateRecognizedTextChatVariable(labelsText)
                else -> mainViewModel.translate(
                    Language.ENGLISH,
                    mainViewModel.language,
                    labelsText
                ).addOnSuccessListener {
                    updateRecognizedTextChatVariable(it)
                }
            }

            // Create list of objects to be shown over the preview
            try {
                requireActivity().runOnUiThread {
                    if (binding.seeingResultsView.height > 0 && binding.seeingResultsView.width > 0) {
                        items = mutableListOf()
                        objects.forEach {
                            var firstLabel = ""
                            if (it.labels.size <= 0 || it.labels[0].confidence < MIN_CONFIDENCE) {
                                return@forEach
                            }

                            // Translate labels if the robot language is other than english and update the text
                            when (mainViewModel.language) {
                                Language.ENGLISH -> firstLabel = it.labels[0].text
                                else -> mainViewModel.translate(
                                    Language.ENGLISH,
                                    mainViewModel.language,
                                    it.labels[0].text
                                ).addOnSuccessListener { firstLabel = it }
                            }
                            // Add the label, the confidence and the bounding box
                            items.add(
                                Recognition(
                                    firstLabel,
                                    it.labels[0].confidence.roundConfidence(),
                                    it.boundingBox.toRectF()
                                )
                            )
                        }
                        // Calculate in which area the objects are and update the bounding boxes
                        showResults(calculateObjectArea(items))
                    }
                }
            } catch (e: Exception) {
                Timber.w("Could not show recognition results due to $e")
            }

            // Take a new image
            this.viewModel.takeImage(mainViewModel.qiContext)
        }

        // Explain Demo Rules
        explainSeeingRules()

        // Take the first image to start
        this.viewModel.takeImage(mainViewModel.qiContext)
    }

    private fun explainSeeingRules() {
        mainViewModel.goToQiChatBookmark(getString(R.string.seeingRulesBookmark))
    }

    private fun togglePreview() {
        binding.seeingPreviewView.toggleVisibility()
    }

    private fun updateRecognizedTextChatVariable(text: String) {
        // Save the results in a variable for them to available in the chat
        mainViewModel.setQiChatVariable(getString(R.string.recognizedInImage), text)
    }

    private fun showResults(objects: List<Recognition>) {
        val recognizedBoundingBox = RecognizedBoundingBox(
            binding.seeingResultsView,
            resources.getColor(R.color.recognizedStrokePaint, requireActivity().theme)
        )
        val recognizedLabelText = RecognizedLabelText(
            resources.displayMetrics,
            resources.getColor(R.color.recognizedStrokePaint, requireActivity().theme)
        )

        // Clear previous results
        binding.seeingResultsView.setImageResource(0)

        // Create canvas on which to draw the results
        val canvas = Canvas(recognizedBoundingBox.mutableBitmap)

        // Draw results: bounding box and label of those with the highest confidence (a maximum of 3 items)
        for (i in objects) {
            if (i.confidence < MIN_CONFIDENCE) {
                continue
            }
            i.location.left += 300
            i.location.right += 300

            // Draw box
            recognizedBoundingBox.drawRect(canvas, i.location)

            // Draw label
            recognizedLabelText.drawText(
                canvas,
                i.location.left,
                i.location.top,
                i.label,
                (i.confidence * 100).toInt()
            )

            // Apply changes
            binding.seeingResultsView.setImageBitmap(recognizedBoundingBox.mutableBitmap)
        }
    }

    // POINTING //////////////////////////////////////////////////////////////////

    fun locateObject() {
        var name = mainViewModel.getQiChatVariable(getString(R.string.objectToLocate))

        // Translate if necessary
        if (mainViewModel.language != Language.ENGLISH) {
            mainViewModel.translate(
                Language.ENGLISH,
                mainViewModel.language,
                name
            ).addOnSuccessListener { name = it }
        }

        // Get the area of the asked object finding the object in the list
        val area =
            items.find {
                it.label == name.lowercase()
            }?.area ?: Area.NONE
        Timber.d("Asked to locate object: $name which is in area: $area")

        doAnimationForTheArea(area)
    }

    private fun calculateObjectArea(objects: List<Recognition>): List<Recognition> {
        for (i in objects) {
            when {
                Range.create(0.0F, (HEIGHT / 3).toFloat())
                    .contains(i.location.centerX()) &&
                    Range.create(0.0F, (HEIGHT / 2).toFloat())
                        .contains(i.location.centerY()) -> {
                    i.area = Area.ONE
                }
                Range.create(
                    (HEIGHT / 3).toFloat(),
                    2 * (HEIGHT / 3).toFloat()
                )
                    .contains(i.location.centerX()) &&
                    Range.create(0.0F, (HEIGHT / 2).toFloat())
                        .contains(i.location.centerY()) -> {
                    i.area = Area.TWO
                }
                Range.create(2 * (HEIGHT / 3).toFloat(), HEIGHT.toFloat())
                    .contains(i.location.centerX()) &&
                    Range.create(0.0F, (HEIGHT / 2).toFloat())
                        .contains(i.location.centerY()) -> {
                    i.area = Area.THREE
                }
                Range.create(0.0F, (HEIGHT / 3).toFloat())
                    .contains(i.location.centerX()) &&
                    Range.create(
                        (HEIGHT / 2).toFloat(),
                        HEIGHT.toFloat()
                    )
                        .contains(i.location.centerY()) -> {
                    i.area = Area.FOUR
                }
                Range.create(
                    (HEIGHT / 3).toFloat(),
                    2 * (HEIGHT / 3).toFloat()
                )
                    .contains(i.location.centerX()) &&
                    Range.create(
                        (HEIGHT / 2).toFloat(),
                        HEIGHT.toFloat()
                    )
                        .contains(i.location.centerY()) -> {
                    i.area = Area.FIVE
                }
                Range.create(2 * (HEIGHT / 3).toFloat(), HEIGHT.toFloat())
                    .contains(i.location.centerX()) &&
                    Range.create(
                        (HEIGHT / 2).toFloat(),
                        HEIGHT.toFloat()
                    )
                        .contains(i.location.centerY()) -> {
                    i.area = Area.SIX
                }
                else -> i.area = Area.NONE
            }
        }
        return objects
    }

    private fun doAnimationForTheArea(area: Area) {
        val animation = when (area) {
            Area.ONE -> R.raw.raise_left_hand_b006
            Area.TWO -> R.raw.raise_right_hand_b007
            Area.THREE -> R.raw.raise_right_hand_b006
            Area.FOUR -> R.raw.raise_left_hand_a003
            Area.FIVE -> R.raw.raise_both_hands_b001
            Area.SIX -> R.raw.raise_right_hand_a001
            Area.NONE -> null
        }

        animation?.let {
            Timber.d("Doing animation for area: $area")
            mainViewModel.pepperActions.doAnimationAsync(
                requireContext(),
                mainViewModel.qiContext,
                animation
            )
        } ?: run { mainViewModel.goToQiChatBookmark(getString(R.string.notFoundBookmark)) }
    }

    companion object {
        const val MIN_CONFIDENCE = 0.35
    }
}
