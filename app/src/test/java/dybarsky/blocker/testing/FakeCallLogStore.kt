package dybarsky.blocker.testing

import dybarsky.blocker.common.components.CallLogStore
import dybarsky.blocker.common.models.BlockedCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.IOException

class FakeCallLogStore(initial: List<BlockedCall> = emptyList()) : CallLogStore {

    /** Newest first, like the real store. */
    val calls = MutableStateFlow(initial)
    var failLoad = false
    var failAdd = false
    var failClear = false

    override suspend fun add(call: BlockedCall) {
        if (failAdd) throw IOException("add failed")
        calls.update { listOf(call) + it }
    }

    override suspend fun clear() {
        if (failClear) throw IOException("clear failed")
        calls.value = emptyList()
    }

    override fun loadCalls(): Flow<List<BlockedCall>> {
        return if (failLoad) flow { throw IOException("load failed") } else calls
    }
}

fun call(number: String, mask: String = "*", timestamp: Long = 0) = BlockedCall(number, mask, timestamp)
