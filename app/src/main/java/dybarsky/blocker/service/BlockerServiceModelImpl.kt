package dybarsky.blocker.service

import dybarsky.blocker.common.components.CallLogStore
import dybarsky.blocker.common.components.Matcher
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.BlockedCall
import dybarsky.blocker.common.util.runCatchingSuspend
import kotlinx.coroutines.flow.first

class BlockerServiceModelImpl(
    private val store: NumberStore,
    private val callLog: CallLogStore,
    private val matcher: Matcher,
    private val now: () -> Long = System::currentTimeMillis,
) : BlockerServiceModel() {

    override suspend fun screen(phone: String?): Verdict {
        if (phone.isNullOrBlank()) return Verdict.Allow
        return runCatchingSuspend {
            val match = store.loadNumbers().first().firstOrNull { matcher(mask = it.mask, text = phone) }
            if (match != null) Verdict.Block(match.mask) else Verdict.Allow
        }.getOrElse {
            Verdict.Allow
        }
    }

    override suspend fun record(phone: String?, verdict: Verdict) {
        if (verdict !is Verdict.Block || phone.isNullOrBlank()) return
        runCatchingSuspend {
            callLog.add(BlockedCall(number = phone, mask = verdict.mask, timestamp = now()))
        }
    }
}
