package dybarsky.blocker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BlockerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        content = content
    )
}
