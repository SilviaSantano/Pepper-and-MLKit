package de.inovex.pepper.intelligence.mlkit.ui.translating

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.inovex.pepper.intelligence.mlkit.utils.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class TranslatingViewModel @Inject constructor() : ViewModel() {

    sealed class UiEvent {
        object ExplainRules : UiEvent()
    }

    private val _uiEvents: SingleLiveEvent<UiEvent> = SingleLiveEvent()
    val uiEvents: LiveData<UiEvent> = _uiEvents

    fun onRulesClicked() {
        _uiEvents.postValue(UiEvent.ExplainRules)
    }
}
