package dybarsky.blocker.ui.screens.home

import androidx.lifecycle.ViewModel
import dybarsky.blocker.common.models.Number
import kotlinx.coroutines.flow.StateFlow

abstract class HomeViewModel : ViewModel() {
    abstract val stateFlow: StateFlow<HomeState>
    abstract fun deleteNumber(id: Number.Id)
}
