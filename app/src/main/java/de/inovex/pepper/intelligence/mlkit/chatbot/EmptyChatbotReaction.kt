package de.inovex.pepper.intelligence.mlkit.chatbot

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.BaseChatbotReaction
import com.aldebaran.qi.sdk.`object`.conversation.SpeechEngine

/**
 * A ChatbotReaction that does nothing.
 */
class EmptyChatbotReaction(context: QiContext?) : BaseChatbotReaction(context) {
    override fun runWith(speechEngine: SpeechEngine?) {
        // Not used.
    }

    override fun stop() {
        // Not used.
    }
}
