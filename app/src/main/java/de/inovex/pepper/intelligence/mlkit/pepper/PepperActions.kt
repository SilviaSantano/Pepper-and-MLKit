package de.inovex.pepper.intelligence.mlkit.pepper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.actuation.Animation
import com.aldebaran.qi.sdk.`object`.camera.TakePicture
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.`object`.holder.AutonomousAbilitiesType
import com.aldebaran.qi.sdk.`object`.holder.Holder
import com.aldebaran.qi.sdk.`object`.image.TimestampedImageHandle
import com.aldebaran.qi.sdk.builder.*
import com.aldebaran.qi.sdk.conversationalcontentlibrary.askrobotname.AskRobotNameConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.base.AbstractConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.base.ConversationalContentChatBuilder
import com.aldebaran.qi.sdk.conversationalcontentlibrary.datetime.DateTimeConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.farewell.FarewellConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.gesturecontrol.GestureControlConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.greetings.GreetingsConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.navigationcontrol.NavigationControlConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.repeat.RepeatConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.robotabilities.RobotAbilitiesConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.robothardwareinfo.RobotHardwareInfoConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.robotipinfo.RobotIpConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.robotnetworkinfo.RobotNetworkConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.robotpersonalinfo.RobotPersonalInfoConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.volumecontrol.VolumeControlConversationalContent
import de.inovex.pepper.intelligence.mlkit.chatbot.wolframalpha.WolframAlphaChatbot
import de.inovex.pepper.intelligence.mlkit.ui.translating.TextTranslator
import de.inovex.pepper.intelligence.mlkit.utils.Language
import timber.log.Timber
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class PepperActions internal constructor() {
    private var animateFuture: Future<Void>? = null
    private lateinit var takePictureFuture: Future<TakePicture>
    private var timestampedImageHandleFuture: Future<TimestampedImageHandle>? = null
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var chat: Chat
    private var chatFuture: Future<Void>? = null
    private lateinit var localTopicStatus: TopicStatus
    private lateinit var wolframAlphaChatbot: WolframAlphaChatbot

    // //////////////////////////////////////////////////////////////////////////////
    // Chat
    // //////////////////////////////////////////////////////////////////////////////

    fun createTopic(qiContext: QiContext, localTopicResource: Int): Topic {
        return TopicBuilder.with(qiContext).withResource(localTopicResource).build()
    }

    @SuppressLint("SimpleDateFormat")
    fun startChat(
        qiContext: QiContext,
        topic: Topic,
        lexicon: Topic,
        chatReadyListener: () -> Unit,
        language: Language,
        textTranslator: TextTranslator
    ): QiChatbot? {

        try {
            if (chatFuture != null) {
                Timber.i("Not starting conversation because the robot is already chatting.")
                return null
            }

            // Conversational contents
            val conversationalContents: List<AbstractConversationalContent> = listOf(
                // NotUnderstoodConversationalContent(),
                RepeatConversationalContent(),
                AskRobotNameConversationalContent(),
                DateTimeConversationalContent(),
                FarewellConversationalContent(),
                GestureControlConversationalContent(),
                GreetingsConversationalContent(),
                NavigationControlConversationalContent(),
                RobotAbilitiesConversationalContent(),
                RobotHardwareInfoConversationalContent(),
                RobotIpConversationalContent(),
                RobotNetworkConversationalContent(),
                RobotPersonalInfoConversationalContent(),
                // UserNotSpeakingConversationalContent(),
                VolumeControlConversationalContent(),
                RobotIpConversationalContent(),
                RobotNetworkConversationalContent()
            )

            // Create QiChatbot
            val qiChatbot = QiChatbotBuilder
                .with(qiContext)
                .withTopics(listOf(topic, lexicon))
                .build()
            localTopicStatus = qiChatbot!!.topicStatus(topic)

            // Create WolframAlpha chatbot
            wolframAlphaChatbot = WolframAlphaChatbot(qiContext, language, textTranslator)

            // Create Chat action.
            chat = ConversationalContentChatBuilder
                .with(qiContext)
                .withChatbots(listOf(qiChatbot, wolframAlphaChatbot))
                .withConversationalContents(conversationalContents)
                .build()
            Timber.i(
                "Chat - built chat %s",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.GERMAN).format(Date())
            )
            chat.addOnStartedListener {
                chatReadyListener()
            }

            // Chat listeners
            setChatListeners()

            // Run Chat action
            chatFuture = chat.async().run()
            Timber.i(
                "Chat - started conversation in language %s - %s",
                Locale.getDefault().displayLanguage,
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
            )

            return qiChatbot
        } catch (e: Exception) {
            Timber.e("Exception on startChat: %s", e.toString())
            return null
        }
    }

    private fun setChatListeners() {
        chat.addOnListeningChangedListener { aBoolean: Boolean? ->
            Timber.d(
                "chat.onListeningChanged(): %s",
                aBoolean
            )
        }
        chat.addOnSayingChangedListener { phrase: Phrase ->
            Timber.d(
                "chat.onSayingChanged(): %s",
                phrase.text
            )
        }
        chat.addOnHeardListener { phrase: Phrase -> Timber.i("chat.onHeard: %s", phrase.text) }
        chat.addOnNoPhraseRecognizedListener { Timber.d("chat.onNoPhraseRecognized()") }
        chat.addOnNormalReplyFoundForListener { input: Phrase ->
            Timber.d(
                "chat.onNormalReplyFoundFor() phrase.getText() = %s",
                input.text
            )
        }
        chat.addOnFallbackReplyFoundForListener { input: Phrase ->
            Timber.d(
                "chat.onFallbackReplyFoundFor() input.getText() =%s",
                input.text
            )
        }
        chat.addOnNoReplyFoundForListener { input: Phrase ->
            Timber.d(
                "chat.onNoReplyFoundFor() input.getText() = %s",
                input.text
            )
        }
    }

    fun removeChatListeners(qiChatbot: QiChatbot?) {
        Thread {
            if (qiChatbot != null) {
                qiChatbot.removeAllOnBookmarkReachedListeners()
                qiChatbot.removeAllOnEndedListeners()
            }
            chat.removeAllOnStartedListeners()
            chat.removeAllOnListeningChangedListeners()
            chat.removeAllOnSayingChangedListeners()
            chat.removeAllOnHeardListeners()
            chat.removeAllOnNoPhraseRecognizedListeners()
            chat.removeAllOnNormalReplyFoundForListeners()
            chat.removeAllOnFallbackReplyFoundForListeners()
            chat.removeAllOnNoReplyFoundForListeners()
            chatFuture = null
        }
    }

    fun goToBookmarkInTopic(
        topic: Topic,
        chatbot: QiChatbot?,
        bookmarkTag: String,
        importance: AutonomousReactionImportance = AutonomousReactionImportance.HIGH,
        validity: AutonomousReactionValidity = AutonomousReactionValidity.IMMEDIATE
    ) {
        try {
            val bookmark: Bookmark? = topic.bookmarks[bookmarkTag]
            chatbot?.goToBookmark(bookmark, importance, validity)
        } catch (e: Exception) {
            Timber.e(
                "Exception with bookmark %s in gotoBookmarkInTopic: %s",
                bookmarkTag,
                e.toString()
            )
        }
    }

    fun getVariableValue(qichatbot: QiChatbot, variable: String): String {
        var value = ""
        try {
            value = qichatbot.variable(variable).value
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value
    }

    fun setVariableValue(qichatbot: QiChatbot?, variable: String, value: String) {
        try {
            qichatbot?.variable(variable)?.value = value
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // //////////////////////////////////////////////////////////////////////////////
    // Animations
    // //////////////////////////////////////////////////////////////////////////////
    fun doAnimationAsync(
        context: Context,
        qiContext: QiContext,
        resourceId: Int,
        musicResourceId: Int = 0
    ) {
        val holder = holdAbilities(qiContext)

        // Create an animation from the resource.
        AnimationBuilder.with(qiContext)
            .withResources(resourceId)
            .buildAsync()
            .andThenConsume { animation: Animation? ->

                // Create an animate action.
                AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .buildAsync()
                    .andThenConsume { animate: Animate? ->

                        // Music and sounds
                        if (musicResourceId != 0) {
                            animate?.addOnStartedListener {
                                mediaPlayer.release()
                                mediaPlayer = MediaPlayer.create(context, musicResourceId)
                                mediaPlayer.start()
                            }
                        }

                        // Run the animate action asynchronously.
                        animateFuture = animate?.async()?.run()?.andThenConsume {
                            releaseAbilities(holder)
                        }
                    }
            }
    }

    private fun stopAnimation() {
        animateFuture?.requestCancellation()
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
    }

    private fun holdAbilities(qiContext: QiContext?): Holder {
        // Build and store the holder for the abilities.
        val holder: Holder = HolderBuilder.with(qiContext)
            .withAutonomousAbilities(
                AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                AutonomousAbilitiesType.BASIC_AWARENESS,
                AutonomousAbilitiesType.AUTONOMOUS_BLINKING
            )
            .build()

        // Hold the abilities.
        holder.hold()

        return holder
    }

    private fun releaseAbilities(holder: Holder) {
        // Release the holder asynchronously.
        holder.release()
    }

    // //////////////////////////////////////////////////////////////////////////////
    // Pictures
    // //////////////////////////////////////////////////////////////////////////////
    fun takePicture(qiContext: QiContext, completion: (Bitmap) -> Unit) {

        // Take picture asynchronously
        takePictureFuture = TakePictureBuilder.with(qiContext).buildAsync()
        timestampedImageHandleFuture = takePictureFuture.andThenCompose { takePicture ->
            Timber.i("--- Taking picture")
            takePicture.async().run()
        }

        // Get the byte buffer from the picture and cast it to byte array to create the bitmap
        timestampedImageHandleFuture?.andThenConsume { timestampedImageHandle ->
            val buffer: ByteBuffer = timestampedImageHandle.image.value.data
            buffer.rewind()
            val pictureBufferSize: Int = buffer.remaining()
            val pictureArray = ByteArray(pictureBufferSize)
            buffer.get(pictureArray)

            Timber.i("Picture taken ($pictureBufferSize Bytes) ---")

            // Return the bitmap
            completion(BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize))
        }
    }
}
