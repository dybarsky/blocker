package dybarsky.blocker.service

abstract class BlockerServiceModel {
    /** Returns `true` only when [phone] is known and matches a stored mask. Fails open. */
    abstract suspend fun shouldBlock(phone: String?): Boolean
}
