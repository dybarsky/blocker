package dybarsky.blocker.service

import dybarsky.blocker.common.components.Matcher
import dybarsky.blocker.common.components.NumberStore
import kotlinx.coroutines.flow.firstOrNull

class BlockerServiceModelImpl(
    private val store: NumberStore,
    private val matcher: Matcher,
) : BlockerServiceModel() {

    override suspend fun shouldBlock(phone: String): Boolean {
        return runCatching {
            val numbers = store.loadNumbers().firstOrNull() ?: emptyList()
            numbers.any { matcher(mask = it.mask, text = phone) }
        }.getOrElse {
            false
        }
    }
}
