package dybarsky.blocker.ui.screens.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen() {
    val controller = rememberNavController()
    NavHost(
        navController = controller,
        startDestination = Route.Roles,
        builder = { mainNavigationGraph(controller) }
    )
}
