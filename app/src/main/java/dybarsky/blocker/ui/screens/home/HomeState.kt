package dybarsky.blocker.ui.screens.home

import dybarsky.blocker.common.models.Number

sealed interface HomeState {
    object Loading : HomeState
    data class Content(val data: List<Number>) : HomeState
}
