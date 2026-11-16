package dybarsky.blocker.ui.screens.log

import dybarsky.blocker.ui.common.Intention

sealed interface LogIntention : Intention {
    data object Clear : LogIntention
    data object Retry : LogIntention
    data object DismissError : LogIntention
}
