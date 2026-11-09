package dybarsky.blocker.ui.common

interface Intention

fun interface Callback<E : Intention> {
    fun submit(event: E)
}
