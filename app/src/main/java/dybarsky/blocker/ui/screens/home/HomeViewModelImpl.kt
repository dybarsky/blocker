package dybarsky.blocker.ui.screens.home

import androidx.lifecycle.viewModelScope
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.ui.screens.home.HomeState.Loading
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModelImpl(
    private val store: NumberStore
) : HomeViewModel() {

    override val stateFlow: StateFlow<HomeState> =
        store.loadNumbers()
            .map { HomeState.Content(data = it) }
            .catch { /* todo */ }
            .stateIn(
                scope = viewModelScope,
                started = Lazily,
                initialValue = Loading
            )

    override fun deleteNumber(id: Number.Id) {
        viewModelScope.launch {
            runCatching {
                store.deleteNumber(id)
            }.onFailure {
                // todo
            }
        }
    }
}
