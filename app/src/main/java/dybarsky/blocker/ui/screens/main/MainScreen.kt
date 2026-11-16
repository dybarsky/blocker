package dybarsky.blocker.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dybarsky.blocker.R
import dybarsky.blocker.ui.common.FloatingNavBar
import dybarsky.blocker.ui.common.FloatingNavItem

private data class Tab(val route: Route, val item: FloatingNavItem)

private val tabs = listOf(
    Tab(Route.Home, FloatingNavItem(label = R.string.nav_masks, icon = R.drawable.ic_block)),
    Tab(Route.Log, FloatingNavItem(label = R.string.log_title, icon = R.drawable.ic_history)),
)

@Composable
fun MainScreen() {
    val controller = rememberNavController()
    // visibleEntries still contains the screen under a dialog, so the bar stays while editing.
    val visible by controller.visibleEntries.collectAsStateWithLifecycle()
    val current = visible.lastOrNull { entry -> tabs.any { entry.destination.hasRoute(it.route::class) } }
    val selectedIndex = tabs.indexOfFirst { current?.destination?.hasRoute(it.route::class) == true }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = controller,
            startDestination = Route.Roles,
            builder = { mainNavigationGraph(controller) }
        )
        if (selectedIndex >= 0) {
            FloatingNavBar(
                items = tabs.map { it.item },
                selectedIndex = selectedIndex,
                onSelect = { index ->
                    if (index != selectedIndex) {
                        controller.navigate(tabs[index].route) {
                            popUpTo<Route.Home> { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            )
        }
    }
}
