package dybarsky.blocker.ui.common

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dybarsky.blocker.ui.theme.BlockerTheme

/** Phone-sized preview on the app's dark background (the app is dark-only). */
@Preview(
    showBackground = true,
    backgroundColor = 0xFF111318,
    widthDp = 360,
    heightDp = 720,
)
annotation class ScreenPreview

/** Same wrapping as `BlockerActivity`: app theme + root [Surface]. */
@Composable
fun PreviewFrame(content: @Composable () -> Unit) {
    BlockerTheme {
        Surface(content = content)
    }
}
