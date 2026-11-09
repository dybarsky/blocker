package dybarsky.blocker.common.components

import dybarsky.blocker.common.models.Number
import kotlinx.coroutines.flow.Flow

interface NumberStore {
    suspend fun saveNumber(number: Number)
    suspend fun deleteNumber(id: Number.Id)
    fun loadNumbers(): Flow<List<Number>>
}
