package de.inovex.pepper.intelligence.mlkit.ui.reading

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import timber.log.Timber

class ImageTextAnalyzer {

    fun extractTextFromImage(input: Bitmap, completion: (String?) -> Unit) {

        val image = InputImage.fromBitmap(input, 0)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Extract the recognition results
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                Timber.i("ImageAnalyzer recognized text: ${visionText.text}")
                completion(visionText.text.replace("\n", " "))
            }
            .addOnFailureListener { e ->
                Timber.e("Error processing the image: $e")
                completion(null)
            }
    }
}
