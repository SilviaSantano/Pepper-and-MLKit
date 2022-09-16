package de.inovex.pepper.intelligence.mlkit.ui.seeing

import android.graphics.Bitmap
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import de.inovex.pepper.intelligence.mlkit.utils.MAX_RESULT_DISPLAY
import timber.log.Timber

class ImageAnalyzer {

    fun analyzeImageWithMLKitObjectDetector(
        picture: Bitmap,
        localModel: LocalModel,
        completion: (List<DetectedObject>?) -> Unit
    ) {

        val image = InputImage.fromBitmap(picture, 0)
        val options = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        val objectDetector = ObjectDetection.getClient(options)

        // Extract the recognition results
        objectDetector.process(image)
            .addOnSuccessListener { detectedObjects ->
                Timber.i("ImageAnalyzer found ${detectedObjects.size} objects")
                for (o in detectedObjects) {
                    Timber.i("ImageAnalyzer  Object: ${detectedObjects.indexOf(o)}")
                    for (l in o.labels) {
                        Timber.i("ImageAnalyzer    ${l.text}")
                    }
                }

                completion(
                    detectedObjects.onEach { detectedObject ->
                        detectedObject.labels.sortByDescending { it.confidence }
                    }.take(MAX_RESULT_DISPLAY).toList()
                )
            }
            .addOnFailureListener { e ->
                Timber.e("Error processing the image: $e")
                completion(null)
            }
    }
}
