package dybarsky.blocker.ui.screens.home

import androidx.lifecycle.viewModelScope
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.common.util.runCatchingSuspend
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModelImpl(
    private val store: NumberStore
) : HomeViewModel() {

    private val state = MutableStateFlow<HomeState>(HomeState.Loading)
    private var loadJob: Job? = null

    override val stateFlow: StateFlow<HomeState> = state

    init {
        load()
    }

    override fun retry() = load()

    override fun deleteNumber(id: Number.Id) {
        viewModelScope.launch {
            runCatchingSuspend { store.deleteNumber(id) }
                .onFailure { setDeleteFailed(true) }
        }
    }

    override fun dismissError() = setDeleteFailed(false)

    private fun load() {
        loadJob?.cancel()
        state.value = HomeState.Loading
        loadJob = viewModelScope.launch {
            store.loadNumbers()
                .catch { state.value = HomeState.Error }
                .collect { numbers ->
                    state.update { current ->
                        val failed = (current as? HomeState.Content)?.deleteFailed ?: false
                        HomeState.Content(data = numbers, deleteFailed = failed)
                    }
                }
        }
    }

    private fun setDeleteFailed(failed: Boolean) {
        state.update { current ->
            if (current is HomeState.Content) current.copy(deleteFailed = failed) else current
        }
    }
}
