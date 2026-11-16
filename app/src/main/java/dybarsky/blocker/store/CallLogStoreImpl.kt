package dybarsky.blocker.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import dybarsky.blocker.common.components.CallLogStore
import dybarsky.blocker.common.models.BlockedCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallLogStoreImpl(context: Context) : CallLogStore {

    private val Context.dataStore: DataStore<List<CallRecord>> by dataStore(
        fileName = "calls.json",
        serializer = JsonListSerializer(CallRecord.serializer()),
        corruptionHandler = ReplaceFileCorruptionHandler { emptyList() }
    )
    private val context = context.applicationContext

    override suspend fun add(call: BlockedCall) {
        context.dataStore.updateData { it.plusCapped(call.asRecord(), MAX_ENTRIES) }
    }

    override suspend fun clear() {
        context.dataStore.updateData { emptyList() }
    }

    override fun loadCalls(): Flow<List<BlockedCall>> {
        // Stored oldest → newest; exposed newest first.
        return context.dataStore.data.map { list -> list.asReversed().map(CallRecord::asBlockedCall) }
    }

    companion object {
        const val MAX_ENTRIES = 200
    }
}
