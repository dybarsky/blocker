package dybarsky.blocker.ui.screens.edit

import androidx.compose.runtime.Composable
import dybarsky.blocker.ui.common.Callback
import dybarsky.blocker.ui.common.PreviewFrame
import dybarsky.blocker.ui.common.ScreenPreview

private val noop = Callback<EditorIntention> { }

@Composable
private fun Preview(state: EditorState) = PreviewFrame { EditorScreen(state, noop) }

@ScreenPreview
@Composable
private fun EditorNewPreview() = Preview(EditorState.Content(mask = null))

@ScreenPreview
@Composable
private fun EditorEditPreview() = Preview(EditorState.Content(mask = "+49 145 *"))

@ScreenPreview
@Composable
private fun EditorSaveFailedPreview() =
    Preview(EditorState.Content(mask = "+49 145 *", saveFailed = true))

@ScreenPreview
@Composable
private fun EditorLoadingPreview() = Preview(EditorState.Loading)

@ScreenPreview
@Composable
private fun EditorErrorPreview() = Preview(EditorState.Error)
