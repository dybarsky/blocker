package dybarsky.blocker.service

sealed interface Verdict {
    data object Allow : Verdict
    data class Block(val mask: String) : Verdict
}

abstract class BlockerServiceModel {

    /** Decides what to do with a call. Fails open: unknown number or any error → [Verdict.Allow]. */
    abstract suspend fun screen(phone: String?): Verdict

    /** Logs a blocked call. Never throws (except cancellation); a logging failure must not matter. */
    abstract suspend fun record(phone: String?, verdict: Verdict)
}
