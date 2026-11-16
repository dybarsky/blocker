package dybarsky.blocker.ui.screens.log

import androidx.lifecycle.viewModelScope
import dybarsky.blocker.common.components.CallLogStore
import dybarsky.blocker.common.util.runCatchingSuspend
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogViewModelImpl(
    private val callLog: CallLogStore
) : LogViewModel() {

    private val state = MutableStateFlow<LogState>(LogState.Loading)
    private var loadJob: Job? = null

    override val stateFlow: StateFlow<LogState> = state

    init {
        load()
    }

    override fun retry() = load()

    override fun clear() {
        viewModelScope.launch {
            runCatchingSuspend { callLog.clear() }
                .onFailure { setClearFailed(true) }
        }
    }

    override fun dismissError() = setClearFailed(false)

    private fun load() {
        loadJob?.cancel()
        state.value = LogState.Loading
        loadJob = viewModelScope.launch {
            callLog.loadCalls()
                .catch { state.value = LogState.Error }
                .collect { calls ->
                    state.update { current ->
                        val failed = (current as? LogState.Content)?.clearFailed ?: false
                        LogState.Content(items = calls, clearFailed = failed)
                    }
                }
        }
    }

    private fun setClearFailed(failed: Boolean) {
        state.update { current ->
            if (current is LogState.Content) current.copy(clearFailed = failed) else current
        }
    }
}
