package dybarsky.blocker.ui.screens.home

import dybarsky.blocker.common.models.Number
import dybarsky.blocker.ui.common.Intention

sealed interface HomeIntention : Intention {
    data class DeleteNumber(val id: Number.Id) : HomeIntention
    data class EditNumber(val id: Number.Id) : HomeIntention
    data object AddNumber : HomeIntention
}
