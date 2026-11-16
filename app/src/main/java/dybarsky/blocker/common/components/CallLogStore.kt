package dybarsky.blocker.common.components

import dybarsky.blocker.common.models.BlockedCall
import kotlinx.coroutines.flow.Flow

interface CallLogStore {
    suspend fun add(call: BlockedCall)
    suspend fun clear()

    /** Newest call first. */
    fun loadCalls(): Flow<List<BlockedCall>>
}
