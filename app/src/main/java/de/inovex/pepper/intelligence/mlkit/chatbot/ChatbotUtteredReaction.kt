package de.inovex.pepper.intelligence.mlkit.chatbot

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.BaseChatbotReaction
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.conversation.SpeechEngine
import com.aldebaran.qi.sdk.builder.SayBuilder
import timber.log.Timber
import java.util.concurrent.ExecutionException

/**
 * A ChatbotReaction that simply utters its reply.
 *
 */
class ChatbotUtteredReaction(qiContext: QiContext?, private val toBeSaid: String?) :
    BaseChatbotReaction(qiContext) {
    private var fsay: Future<Void?>? = null
    override fun runWith(speechEngine: SpeechEngine?) {
        // All Say actions that must be executed inside this method must be created via the SpeechEngine
        val say: Say = SayBuilder.with(speechEngine)
            .withText(toBeSaid)
            .build()
        try {
            // The say action must be executed asynchronously in order to get
            // a future that can be canceled by the head thanks to the stop() method
            fsay = say.async().run()

            // However, runWith must not be left before the say action is terminated : thus wait on the future
            fsay!!.get()
        } catch (ex: ExecutionException) {
            Timber.e("Error during say: %s", ex.message)
        }
    }

    override fun stop() {

        // All actions created in runWith should be canceled when stop is called
        if (fsay != null) {
            fsay!!.requestCancellation()
        }
    }
}
