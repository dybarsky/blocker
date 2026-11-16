package dybarsky.blocker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Space lists reserve at their end so the last item can scroll above the card FAB. */
val CardFabClearance: Dp = 88.dp

/**
 * Same frame for every top-level screen: system bars, side margins and room for the floating
 * navigation bar. Using it everywhere keeps [ScreenCard] exactly the same size on all screens.
 * Overlays (snackbar, FAB) go directly into this container, next to the card.
 */
@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = FloatingNavBarContentPadding),
        content = content,
    )
}

/** Rounded surface that fills its [ScreenContainer]. */
@Composable
fun ScreenCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceBright),
        content = content,
    )
}

/** Round action button; place it with `Modifier.align(Alignment.BottomEnd)` inside a [ScreenContainer]. */
@Composable
fun CardFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(onClick = onClick, modifier = modifier.padding(16.dp)) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
