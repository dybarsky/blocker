package dybarsky.blocker.ui.screens.edit

sealed interface EditorState {
    object Saved : EditorState
    object Loading : EditorState
    data class Content(val mask: String?) : EditorState
}
