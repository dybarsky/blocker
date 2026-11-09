package dybarsky.blocker.service

abstract class BlockerServiceModel {
    abstract suspend fun shouldBlock(phone: String): Boolean
}
