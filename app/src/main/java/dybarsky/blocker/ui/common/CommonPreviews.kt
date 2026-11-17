package dybarsky.blocker.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dybarsky.blocker.R

private val navItems = listOf(
    FloatingNavItem(label = R.string.nav_masks, icon = R.drawable.ic_block),
    FloatingNavItem(label = R.string.log_title, icon = R.drawable.ic_history),
)

@Preview(showBackground = true, backgroundColor = 0xFF111318, widthDp = 360)
@Composable
private fun FloatingNavBarPreview() = PreviewFrame {
    Column(Modifier.padding(16.dp)) {
        FloatingNavBar(navItems, selectedIndex = 0, onSelect = {})
        Spacer(Modifier.height(8.dp))
        FloatingNavBar(navItems, selectedIndex = 1, onSelect = {})
    }
}

@ScreenPreview
@Composable
private fun ScreenCardWithFabPreview() = PreviewFrame {
    ScreenContainer {
        ScreenCard {
            Text("Card", modifier = Modifier.align(Alignment.Center))
        }
        CardFab(
            icon = Icons.Default.Add,
            contentDescription = "Add",
            onClick = {},
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}
