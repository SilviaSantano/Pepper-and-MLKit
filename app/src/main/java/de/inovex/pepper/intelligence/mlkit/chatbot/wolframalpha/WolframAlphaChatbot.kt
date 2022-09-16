package de.inovex.pepper.intelligence.mlkit.chatbot.wolframalpha

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.`object`.locale.Locale
import de.inovex.pepper.intelligence.mlkit.chatbot.ChatbotUtteredReaction
import de.inovex.pepper.intelligence.mlkit.chatbot.EmptyChatbotReaction
import de.inovex.pepper.intelligence.mlkit.ui.translating.TextTranslator
import de.inovex.pepper.intelligence.mlkit.utils.Language
import timber.log.Timber

/**
 * A chatbot that delegates questions/answers to an agent.
 */
class WolframAlphaChatbot(
    qiContext: QiContext?,
    private val language: Language,
    private val textTranslator: TextTranslator
) : BaseChatbot(qiContext) {

    override fun replyTo(phrase: Phrase?, locale: Locale?): StandardReplyReaction? {

        return if (phrase?.text!!.isEmpty() || phrase.text == "<...>") {
            Timber.i("WolframAlphaChatbot - Empty query, It will be ignored.")
            val emptyReaction = EmptyChatbotReaction(qiContext)
            StandardReplyReaction(emptyReaction as BaseChatbotReaction, ReplyPriority.FALLBACK)
        } else {
            Timber.i("WolframAlphaChatbot - Query: %s", phrase.text)
            val wolframAlphaAgent: WolframAlphaAgent = WolframAlphaAgent.getInstance(textTranslator)
            val answer = wolframAlphaAgent.getResponseFromWolframAlpha(phrase.text, this.language)
            val reply: StandardReplyReaction? = generateReactionFromWolframAlphaResponse(answer)

            reply
        }
    }

    private fun generateReactionFromWolframAlphaResponse(wolframAlphaResponse: String?): StandardReplyReaction? {

        @Suppress("ComplexCondition")
        if (wolframAlphaResponse == "Wolfram|Alpha did not understand your input" ||
            wolframAlphaResponse == "Wolfram | Alpha hat Ihre Eingabe nicht verstanden" ||
            wolframAlphaResponse == "Wolfram|Alpha hat Ihre Eingabe nicht verstanden" ||
            wolframAlphaResponse == "No short answer available" ||
            wolframAlphaResponse == "Keine kurze Antwort verfügbar" ||
            wolframAlphaResponse == "I don't know why you say goodbye; I say hello" ||
            wolframAlphaResponse == "Ich weiß nicht, warum Sie sich verabschieden. ich sage Hallo" ||
            wolframAlphaResponse == "Ich weiß nicht, warum Sie sich verabschieden." ||
            wolframAlphaResponse == "Hallo Mensch" ||
            wolframAlphaResponse == "Hello, human" ||
            wolframAlphaResponse == "I am doing well, thank you" ||
            wolframAlphaResponse == "Es geht mir gut, danke" ||
            wolframAlphaResponse == "I can help you to compute" ||
            wolframAlphaResponse == "Ich kann Ihnen beim Rechnen helfen" ||
            wolframAlphaResponse == "My name is Wolfram Alpha" ||
            wolframAlphaResponse == "Ich heiße Wolfram Alpha" ||
            wolframAlphaResponse == "an affirmative" ||
            wolframAlphaResponse == "eine Bestätigung" ||
            wolframAlphaResponse == "not any; not one; not a; not allowed; not possible to know what will happen" ||
            wolframAlphaResponse == "keine; nicht eins; kein; nicht erlaubt; nicht möglich zu wissen, " +
            "was passieren wird"
        ) {
            val emptyReaction = EmptyChatbotReaction(qiContext)
            return StandardReplyReaction(emptyReaction, ReplyPriority.FALLBACK)
        }

        val reaction = ChatbotUtteredReaction(qiContext, wolframAlphaResponse)
        return StandardReplyReaction(reaction as BaseChatbotReaction, ReplyPriority.NORMAL)
    }
}
