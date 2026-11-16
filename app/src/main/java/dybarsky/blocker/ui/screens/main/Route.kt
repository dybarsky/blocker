package dybarsky.blocker.ui.screens.main

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object Roles : Route

    @Serializable
    data object Log : Route

    @Serializable
    data class Editor(val id: Long? = null) : Route
}
