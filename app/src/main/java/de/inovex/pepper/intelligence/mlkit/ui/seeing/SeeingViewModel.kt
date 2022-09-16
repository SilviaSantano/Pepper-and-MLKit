package de.inovex.pepper.intelligence.mlkit.ui.seeing

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aldebaran.qi.sdk.QiContext
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.DetectedObject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.inovex.pepper.intelligence.mlkit.pepper.PepperActions
import de.inovex.pepper.intelligence.mlkit.utils.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel, where the recognition results will be stored and updated with each new image
 * analyzed by MLKit.
 */
@HiltViewModel
class SeeingViewModel @Inject constructor(
    private val pepperActions: PepperActions,
    private val imageAnalyzer: ImageAnalyzer
) : ViewModel() {

    sealed class UiEvent {
        object ExplainRules : UiEvent()
        object TogglePreview : UiEvent()
    }

    private val _uiEvents: SingleLiveEvent<UiEvent> = SingleLiveEvent()
    val uiEvents: LiveData<UiEvent> = _uiEvents

    private var MLKitRecognitionObjects: MutableLiveData<List<DetectedObject>> = MutableLiveData()
    private var image: MutableLiveData<Bitmap> = MutableLiveData()

    fun takeImage(qiContext: QiContext) {
        Timber.d("Taking image")
        pepperActions.takePicture(qiContext) {
            Timber.d("Updating image")
            image.postValue(it)
        }
    }

    fun analyzeImageWithMLKitObjectDetector(localModel: LocalModel, image: Bitmap) {
        imageAnalyzer.analyzeImageWithMLKitObjectDetector(
            image,
            localModel
        ) { recognizedLabels ->
            Timber.d("Updating ${recognizedLabels?.size} labels")
            if (recognizedLabels != null) MLKitRecognitionObjects.value = recognizedLabels
            else MLKitRecognitionObjects.value = listOf(
                DetectedObject(
                    Rect(),
                    0,
                    listOf(DetectedObject.Label("Nothing recognized", 0.0F, 0))
                )
            )
        }
    }

    fun getMLKitRecognitionObjects(): MutableLiveData<List<DetectedObject>> {
        return MLKitRecognitionObjects
    }

    fun getLastImage(): LiveData<Bitmap> {
        return image
    }

    fun onRulesClicked() {
        _uiEvents.postValue(UiEvent.ExplainRules)
    }

    fun onPreviewClicked() {
        _uiEvents.postValue(UiEvent.TogglePreview)
    }
}
