package dybarsky.blocker.ui.screens.log

import dybarsky.blocker.common.models.BlockedCall

sealed interface LogState {
    data object Loading : LogState
    data object Error : LogState
    data class Content(
        val items: List<BlockedCall>,
        val clearFailed: Boolean = false,
    ) : LogState
}
