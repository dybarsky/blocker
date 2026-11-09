package dybarsky.blocker.ui.screens.edit

import dybarsky.blocker.ui.common.Intention

sealed interface EditorIntention : Intention {
    data class Save(val mask: String) : EditorIntention
    data object Close : EditorIntention
}
