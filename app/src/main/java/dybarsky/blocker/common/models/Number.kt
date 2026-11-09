package dybarsky.blocker.common.models

data class Number(
    val id: Id,
    val mask: String
) {

    @JvmInline
    value class Id(val value: Long) {
        companion object {
            val New = Id(-1)
        }
    }
}
