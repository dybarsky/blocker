package dybarsky.blocker.ui.screens.edit

sealed interface EditorState {
    data object Saved : EditorState
    data object Loading : EditorState
    data object Error : EditorState
    data class Content(
        val mask: String?,
        val saveFailed: Boolean = false,
    ) : EditorState
}
