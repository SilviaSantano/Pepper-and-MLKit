package de.inovex.pepper.intelligence.mlkit.chatbot.wolframalpha

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import de.inovex.pepper.intelligence.mlkit.ui.translating.TextTranslator
import de.inovex.pepper.intelligence.mlkit.utils.Language
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException

/**
 * Singleton aimed at accessing a specific agent dedicated to this sample.
 */
class WolframAlphaAgent private constructor(private val textTranslator: TextTranslator) {

    /**
     * Ask a question to the chatbot. Translate it first if required.
     * @param question the text we expect to answer to
     * @return the Dialogflow response
     */
    fun getResponseFromWolframAlpha(question: String, language: Language): String {
        var reply: String = ANSWER_NOT_AVAILABLE
        var translateQuestionTask: Task<String>? = null

        when (language) {
            // Make request
            Language.ENGLISH -> {
                reply = queryWolframAlpha(question)
            }

            // Make request, translating to English the question
            Language.GERMAN, Language.SPANISH -> {
                translateQuestionTask =
                    textTranslator.translate(language, Language.ENGLISH, question)
            }
            else -> {}
        }

        if (translateQuestionTask != null) {
            Tasks.await(translateQuestionTask)

            if (translateQuestionTask.isSuccessful) {
                Timber.d("WolframAlpha agent: translated question: ${translateQuestionTask.result}")
                reply = queryWolframAlpha(translateQuestionTask.result)

                // Translate answer to English
                if (reply != ANSWER_NOT_AVAILABLE) {
                    val translateAnswerTask =
                        textTranslator.translate(Language.ENGLISH, language, reply)
                    Tasks.await(translateAnswerTask)
                    Timber.d("WolframAlpha agent: translated answer: ${translateAnswerTask.result}")
                    reply = translateAnswerTask.result
                }
            }
        }

        return reply
    }

    private fun queryWolframAlpha(question: String): String {
        val answer: String

        Timber.i("WolframAlpha get response to question: $question")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(WOLFRAMALPHA_URL + question)
            .build()

        answer = try {
            val response = client.newCall(request).execute()
            Timber.i("WolframAlpha response: $response")
            response.body()?.string() ?: ANSWER_NOT_AVAILABLE
        } catch (exception: IOException) {
            Timber.e("WolframAlpha encountered an error processing the request: $exception")
            ANSWER_NOT_AVAILABLE
        }
        return answer
    }

    companion object {
        private const val WOLFRAMALPHA_URL: String =
            "http://api.wolframalpha.com/v1/result?appid=AV7QA2-E8Q4PT4L8G&units=metric&i="
        const val ANSWER_NOT_AVAILABLE: String = "No short answer available"
        private lateinit var INSTANCE: WolframAlphaAgent

        /**
         * @return the unique instance of this class
         */
        fun getInstance(textTranslator: TextTranslator): WolframAlphaAgent {
            INSTANCE = WolframAlphaAgent(textTranslator)
            return INSTANCE
        }
    }
}
