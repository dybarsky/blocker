package dybarsky.blocker.testing

import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.Number
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.IOException

class FakeNumberStore(initial: List<Number> = emptyList()) : NumberStore {

    val numbers = MutableStateFlow(initial)
    var failLoad = false
    var failSave = false
    var failDelete = false
    /** When true, `loadNumbers()` emits nothing until [release] is called. */
    var holdLoad = false
    private val released = MutableStateFlow(false)
    var saveCalls = 0
        private set

    override suspend fun saveNumber(number: Number) {
        if (failSave) throw IOException("save failed")
        saveCalls++
        numbers.update { list ->
            val index = list.indexOfFirst { it.id == number.id }
            when {
                index >= 0 -> list.toMutableList().also { it[index] = number }
                else -> list + number.copy(id = Number.Id(list.size + 1L))
            }
        }
    }

    override suspend fun deleteNumber(id: Number.Id) {
        if (failDelete) throw IOException("delete failed")
        numbers.update { list -> list.filterNot { it.id == id } }
    }

    override fun loadNumbers(): Flow<List<Number>> {
        return when {
            failLoad -> flow { throw IOException("load failed") }
            holdLoad -> flow {
                released.first { it }
                emitAll(numbers)
            }
            else -> numbers
        }
    }

    fun release() {
        released.value = true
    }
}

fun number(id: Long, mask: String) = Number(Number.Id(id), mask)
