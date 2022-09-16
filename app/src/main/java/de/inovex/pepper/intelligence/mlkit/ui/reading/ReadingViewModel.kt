package de.inovex.pepper.intelligence.mlkit.ui.reading

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aldebaran.qi.sdk.QiContext
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
class ReadingViewModel @Inject constructor(
    private val pepperActions: PepperActions,
    private val imageTextAnalyzer: ImageTextAnalyzer
) : ViewModel() {

    sealed class UiEvent {
        object ExplainRules : UiEvent()
        object TogglePreview : UiEvent()
    }

    private val _uiEvents: SingleLiveEvent<UiEvent> = SingleLiveEvent()
    val uiEvents: LiveData<UiEvent> = _uiEvents
    private var extractedText: MutableLiveData<String> = MutableLiveData()
    private var image: MutableLiveData<Bitmap> = MutableLiveData()

    fun takeImage(qiContext: QiContext) {
        Timber.d("Taking image")
        pepperActions.takePicture(qiContext) {
            Timber.d("Updating image")
            image.postValue(it)
        }
    }

    fun extractTextFromImage(image: Bitmap) {
        imageTextAnalyzer.extractTextFromImage(image) { visionText ->
            Timber.i(visionText)
            if (!visionText.isNullOrBlank()) extractedText.value = visionText
            else extractedText.value = "Nothing recognized"
        }
    }

    fun getReadText(): LiveData<String> {
        return extractedText
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
