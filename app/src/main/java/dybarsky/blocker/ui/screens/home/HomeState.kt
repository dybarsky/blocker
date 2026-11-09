package dybarsky.blocker.ui.screens.home

import dybarsky.blocker.common.models.Number

sealed interface HomeState {
    data object Loading : HomeState
    data object Error : HomeState
    data class Content(
        val data: List<Number>,
        val deleteFailed: Boolean = false,
    ) : HomeState
}
