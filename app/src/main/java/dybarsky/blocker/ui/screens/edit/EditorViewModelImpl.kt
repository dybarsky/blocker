package dybarsky.blocker.ui.screens.edit

import androidx.lifecycle.viewModelScope
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.common.util.runCatchingSuspend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditorViewModelImpl(
    private val id: Number.Id?,
    private val store: NumberStore
) : EditorViewModel() {

    private val state = MutableStateFlow<EditorState>(
        if (id == null) EditorState.Content(mask = null) else EditorState.Loading
    )

    override val stateFlow: StateFlow<EditorState> = state

    init {
        if (id != null) loadNumber(id)
    }

    override fun saveNumber(mask: String) {
        viewModelScope.launch {
            if (mask.isBlank()) {
                state.value = EditorState.Saved
                return@launch
            }
            setSaveFailed(false)
            runCatchingSuspend {
                store.saveNumber(Number(id = id ?: Number.Id.New, mask = mask))
            }.onSuccess {
                state.value = EditorState.Saved
            }.onFailure {
                setSaveFailed(true)
            }
        }
    }

    private fun loadNumber(id: Number.Id) {
        viewModelScope.launch {
            runCatchingSuspend {
                store.loadNumbers().first().firstOrNull { it.id == id }
            }.onSuccess { number ->
                state.value = if (number != null) EditorState.Content(number.mask) else EditorState.Error
            }.onFailure {
                state.value = EditorState.Error
            }
        }
    }

    private fun setSaveFailed(failed: Boolean) {
        state.update { current ->
            if (current is EditorState.Content) current.copy(saveFailed = failed) else current
        }
    }
}
