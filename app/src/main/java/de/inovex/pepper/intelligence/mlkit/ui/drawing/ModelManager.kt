package de.inovex.pepper.intelligence.mlkit.ui.drawing

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import timber.log.Timber

/** Class to manage model downloading, deletion, and selection.  */
class ModelManager {
    private var model: DigitalInkRecognitionModel? = null
    var recognizer: DigitalInkRecognizer? = null
    private val remoteModelManager = RemoteModelManager.getInstance()

    fun checkIsModelDownloaded(): Task<Boolean?> {
        return remoteModelManager.isModelDownloaded(model!!)
    }

    fun downloadModel(modelTag: String) {
        // Clear the old model
        model = null

        // Try to parse the languageTag and get a model from it
        val modelIdentifier: DigitalInkRecognitionModelIdentifier? = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag(modelTag)
        } catch (e: MlKitException) {
            null
        }

        // Initialize the model and download model if not downloaded yet
        modelIdentifier?.let {
            model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
            remoteModelManager.isModelDownloaded(model!!)
                .addOnSuccessListener { result: Boolean? ->
                    if (!result!!) {
                        remoteModelManager.download(model!!, DownloadConditions.Builder().build())
                    }
                }
                .addOnFailureListener { e: Exception ->
                    Timber.e("Error downloading model: $e")
                }
        }
    }

    fun setRecognizer(modelTag: String) {
        // Try to parse the languageTag and get a model from it.
        val modelIdentifier: DigitalInkRecognitionModelIdentifier? = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag(modelTag)
        } catch (e: MlKitException) {
            null
        }
        recognizer?.close()
        recognizer = null
        modelIdentifier?.let {
            model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
            recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model!!).build()
            )
        }
    }
}
