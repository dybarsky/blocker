package dybarsky.blocker.ui.screens.main

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.ui.screens.edit.EditorScreen
import dybarsky.blocker.ui.screens.home.HomeScreen
import dybarsky.blocker.ui.screens.role.RoleScreen

fun NavGraphBuilder.mainNavigationGraph(controller: NavController) {
    composable<Route.Roles> {
        RoleScreen(
            goToHome = {
                controller.popBackStack(Route.Roles, inclusive = true)
                controller.navigate(Route.Home)
            }
        )
    }
    composable<Route.Home> {
        HomeScreen(
            goToEditor = {
                controller.navigate(Route.Editor(it?.value))
            }
        )
    }
    dialog<Route.Editor> {
        EditorScreen(
            id = it.toRoute<Route.Editor>().id?.let(Number::Id),
            close = { controller.popBackStack() }
        )
    }
}
