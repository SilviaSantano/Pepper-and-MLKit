package de.inovex.pepper.intelligence.mlkit.ui.drawing

import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.Ink
import de.inovex.pepper.intelligence.mlkit.ui.drawing.RecognitionTask.RecognizedInk
import timber.log.Timber
import java.util.*

/** Manages the recognition logic and the content that has been added to the current page.  */
class StrokeManager {
    /** Interface to register to be notified of changes in the recognized content.  */
    interface ContentChangedListener {
        /** This method is called when the recognized content changes.  */
        fun onDrawingContentChanged()
    }

    // For handling recognition and model downloading.
    private var recognitionTask: RecognitionTask? = null

    @JvmField
    @VisibleForTesting
    var modelManager =
        ModelManager()

    // Managing the recognition queue.
    private val content: MutableList<RecognizedInk> = ArrayList()

    // Managing ink currently drawn.
    private var strokeBuilder = Ink.Stroke.builder()
    private var inkBuilder = Ink.builder()
    private var stateChangedSinceLastRequest = false
    private var contentChangedListener: ContentChangedListener? = null
    private var triggerRecognitionAfterInput = true
    private var clearCurrentInkAfterRecognition = true

    fun setTriggerRecognitionAfterInput(shouldTrigger: Boolean) {
        triggerRecognitionAfterInput = shouldTrigger
    }

    fun setClearCurrentInkAfterRecognition(shouldClear: Boolean) {
        clearCurrentInkAfterRecognition = shouldClear
    }

    // Handler to handle the UI Timeout.
    // This handler is only used to trigger the UI timeout. Each time a UI interaction happens,
    // the timer is reset by clearing the queue on this handler and sending a new delayed message (in
    // addNewTouchEvent).
    private val uiHandler = Handler(
        Handler.Callback { msg: Message ->
            if (msg.what == TIMEOUT_TRIGGER) {
                Timber.d("Handling timeout trigger.")
                commitResult()
                return@Callback true
            }
            false
        }
    )

    private fun commitResult() {
        Timber.d("commitResult")
        recognitionTask!!.result()?.let {
            content.add(it)
            if (clearCurrentInkAfterRecognition) {
                resetCurrentInk()
            }

            Timber.d("setContentChangedListener")
            contentChangedListener?.onDrawingContentChanged()
        }
    }

    fun reset() {
        Timber.d("reset")
        resetCurrentInk()
        content.clear()
        recognitionTask?.cancel()
    }

    private fun resetCurrentInk() {
        inkBuilder = Ink.builder()
        strokeBuilder = Ink.Stroke.builder()
        stateChangedSinceLastRequest = false
    }

    val currentInk: Ink
        get() = inkBuilder.build()

    /**
     * This method is called when a new touch event happens on the drawing client and notifies the
     * StrokeManager of new content being added.
     *
     *
     * This method takes care of triggering the UI timeout and scheduling recognitions on the
     * background thread.
     *
     * @return whether the touch event was handled.
     */
    fun addNewTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val x = event.x
        val y = event.y
        val t = System.currentTimeMillis()

        // A new event happened -> clear all pending timeout messages.
        uiHandler.removeMessages(TIMEOUT_TRIGGER)
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> strokeBuilder.addPoint(
                Ink.Point.create(
                    x,
                    y,
                    t
                )
            )
            MotionEvent.ACTION_UP -> {
                strokeBuilder.addPoint(Ink.Point.create(x, y, t))
                inkBuilder.addStroke(strokeBuilder.build())
                strokeBuilder = Ink.Stroke.builder()
                stateChangedSinceLastRequest = true
                if (triggerRecognitionAfterInput) {
                    recognize()
                }
            }
            else -> // Indicate touch event wasn't handled.
                return false
        }
        return true
    }

    // Listeners to update the drawing and status.
    fun setContentChangedListener(contentChangedListener: ContentChangedListener?) {
        this.contentChangedListener = contentChangedListener
    }

    fun getContent(): List<RecognizedInk> {
        return content
    }

    // Model downloading / deleting / setting.
    fun setActiveModel(languageTag: String) {
        modelManager.setRecognizer(languageTag)
    }

    fun downloadModels() {
        modelManager.downloadModel("zxx-Zsym-x-autodraw")
        modelManager.downloadModel("en")
        modelManager.downloadModel("de")
        modelManager.downloadModel("es")
    }

    // Recognition-related.
    fun recognize(): Task<String?> {
        if (!stateChangedSinceLastRequest || inkBuilder.isEmpty) {
            return Tasks.forResult(null)
        }
        if (modelManager.recognizer == null) {
            return Tasks.forResult(null)
        }
        return modelManager
            .checkIsModelDownloaded()
            .onSuccessTask { result: Boolean? ->
                Timber.d("model IS DOWNLOADED")
                if (!result!!) {
                    return@onSuccessTask Tasks.forResult<String?>(
                        null
                    )
                }
                stateChangedSinceLastRequest = false
                recognitionTask =
                    RecognitionTask(
                        modelManager.recognizer,
                        inkBuilder.build()
                    )
                uiHandler.sendMessageDelayed(
                    uiHandler.obtainMessage(TIMEOUT_TRIGGER),
                    CONVERSION_TIMEOUT_MS
                )
                Timber.d("task running")
                recognitionTask!!.run()
            }
    }

    companion object {
        @VisibleForTesting
        const val CONVERSION_TIMEOUT_MS: Long = 1000
        private const val TAG = "MLKD.StrokeManager"

        // This is a constant that is used as a message identifier to trigger the timeout.
        private const val TIMEOUT_TRIGGER = 1
    }
}
