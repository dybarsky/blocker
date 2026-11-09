package dybarsky.blocker.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.Number
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NumberStoreImpl(context: Context) : NumberStore {

    private val Context.dataStore: DataStore<List<Record>> by dataStore(
        fileName = "numbers.json",
        serializer = RecordSerializer
    )
    private val context = context.applicationContext

    override suspend fun saveNumber(number: Number) {
        context.dataStore.updateData {
            val list = it.toMutableList()
            val index = list.indexOfFirst { record -> record.id == number.id.value }
            val record = number.asRecord()
            if (index >= 0) list[index] = record else list.add(record)
            list.toList()
        }
    }

    override suspend fun deleteNumber(id: Number.Id) {
        context.dataStore.updateData {
            val list = it.toMutableList()
            list.removeIf { record -> record.id == id.value }
            list.toList()
        }
    }

    override fun loadNumbers(): Flow<List<Number>> {
        return context.dataStore.data.map { it.map(Record::asNumber) }
    }
}
