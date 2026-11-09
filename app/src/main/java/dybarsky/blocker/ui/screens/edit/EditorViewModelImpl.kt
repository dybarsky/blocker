package dybarsky.blocker.ui.screens.edit

import androidx.lifecycle.viewModelScope
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.ui.screens.edit.EditorState.Loading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class EditorViewModelImpl(
    private val id: Number.Id?,
    private val store: NumberStore
) : EditorViewModel() {

    override val stateFlow = MutableStateFlow<EditorState>(Loading)

    init {
        val flow = if (id == null) {
            flowOf(null)
        } else {
            store.loadNumbers()
                .mapNotNull { it.firstOrNull { item -> item.id == id } }
                .map { it.mask }
        }
        viewModelScope.launch {
            flow.map { EditorState.Content(it) }
                .catch { /* todo */ }
                .onEach { stateFlow.emit(it) }
                .collect()
        }
    }

    override fun saveNumber(mask: String) {
        viewModelScope.launch {
            if (mask.isBlank()) {
                stateFlow.emit(EditorState.Saved)
                return@launch
            }
            runCatching {
                val number = Number(
                    id = id ?: Number.Id.New,
                    mask = mask
                )
                store.saveNumber(number)
                stateFlow.emit(EditorState.Saved)
            }.onFailure {
                // todo
            }
        }
    }
}
