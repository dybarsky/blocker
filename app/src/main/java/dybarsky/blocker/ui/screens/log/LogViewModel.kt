package dybarsky.blocker.ui.screens.log

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class LogViewModel : ViewModel() {
    abstract val stateFlow: StateFlow<LogState>
    abstract fun clear()
    abstract fun retry()
    abstract fun dismissError()
}
