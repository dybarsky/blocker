package dybarsky.blocker.service

import dybarsky.blocker.common.components.Matcher
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.util.runCatchingSuspend
import kotlinx.coroutines.flow.first

class BlockerServiceModelImpl(
    private val store: NumberStore,
    private val matcher: Matcher,
) : BlockerServiceModel() {

    override suspend fun shouldBlock(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        return runCatchingSuspend {
            store.loadNumbers().first().any { matcher(mask = it.mask, text = phone) }
        }.getOrElse {
            false
        }
    }
}
