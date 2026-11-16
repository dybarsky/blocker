package dybarsky.blocker.common.models

/** A call that was rejected by the app: who called, which mask matched, and when (epoch millis). */
data class BlockedCall(
    val number: String,
    val mask: String,
    val timestamp: Long,
)
