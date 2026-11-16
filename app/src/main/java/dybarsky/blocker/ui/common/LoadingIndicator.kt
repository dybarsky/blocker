package dybarsky.blocker.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dybarsky.blocker.R
import kotlinx.coroutines.delay

private const val SHOW_DELAY_MS = 250L

/**
 * Centered progress spinner. It appears only after a short delay so that
 * near-instant loads (local DataStore) don't flash a spinner. The container
 * keeps its size meanwhile, so nothing jumps when content arrives.
 */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(SHOW_DELAY_MS)
        visible = true
    }
    val description = stringResource(R.string.loading)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (visible) {
            CircularProgressIndicator(
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}
