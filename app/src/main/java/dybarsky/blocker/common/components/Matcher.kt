package dybarsky.blocker.common.components

interface Matcher {
    operator fun invoke(mask: String, text: String): Boolean
}
