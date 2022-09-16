package de.inovex.pepper.intelligence.mlkit.ui.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.aldebaran.qi.sdk.`object`.conversation.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import de.inovex.pepper.intelligence.R
import de.inovex.pepper.intelligence.mlkit.pepper.PepperActions
import de.inovex.pepper.intelligence.mlkit.ui.translating.TextTranslator
import de.inovex.pepper.intelligence.mlkit.utils.Language
import de.inovex.pepper.intelligence.mlkit.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor(
    private val textTranslator: TextTranslator,
    val pepperActions: PepperActions
) : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    lateinit var qiContext: QiContext
    lateinit var mainTopic: Topic
    lateinit var lexiconTopic: Topic
    lateinit var language: Language
    var qiChatbot: QiChatbot? = null

    sealed class UiEvent {
        object NavigateToReading : UiEvent()
        object NavigateToSeeing : UiEvent()
        object NavigateToTranslating : UiEvent()
        object NavigateToDrawing : UiEvent()
        object ExplainDemoRules : UiEvent()
    }

    private val _uiEvents: SingleLiveEvent<UiEvent> = SingleLiveEvent()
    val uiEvents: LiveData<UiEvent> = _uiEvents

    fun onDrawingClicked() {
        _uiEvents.postValue(UiEvent.NavigateToDrawing)
    }

    fun onSeeingClicked() {
        _uiEvents.postValue(UiEvent.NavigateToSeeing)
    }

    fun onReadingClicked() {
        _uiEvents.postValue(UiEvent.NavigateToReading)
    }

    fun onTranslatingClicked() {
        _uiEvents.postValue(UiEvent.NavigateToTranslating)
    }

    fun onDemoRulesClicked() {
        _uiEvents.postValue(UiEvent.ExplainDemoRules)
    }

    fun translate(source: Language, target: Language, text: String) = textTranslator.translate(
        source, target, text
    )

    fun startChat(chatReadyListener: () -> Unit) {
        mainTopic = pepperActions.createTopic(qiContext, R.raw.topic_intelligence)
        lexiconTopic = pepperActions.createTopic(qiContext, R.raw.topic_softbank_lexicon)
        qiChatbot = pepperActions.startChat(
            qiContext,
            mainTopic,
            lexiconTopic,
            chatReadyListener,
            language,
            textTranslator
        )
    }

    fun goToQiChatBookmark(bookmark: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pepperActions.goToBookmarkInTopic(
                mainTopic,
                qiChatbot,
                bookmark
            )
        }
    }

    fun getQiChatVariable(variable: String): String {
        return pepperActions.getVariableValue(
            qiChatbot!!,
            variable
        )
    }

    fun setQiChatVariable(variable: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pepperActions.setVariableValue(
                qiChatbot,
                variable,
                value
            )
        }
    }

    fun performHappyPepperAnimation(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            pepperActions.doAnimationAsync(
                context,
                qiContext,
                R.raw.raise_both_hands_b001,
                0
            )
        }
    }
}
