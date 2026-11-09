package dybarsky.blocker.service

import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.domain.MatcherImpl
import dybarsky.blocker.testing.FakeNumberStore
import dybarsky.blocker.testing.number
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class BlockerServiceModelImplTest {

    private val store = FakeNumberStore(listOf(number(1, "+38050*")))
    private val model = BlockerServiceModelImpl(store, MatcherImpl())

    @Test
    fun matchingNumber_isBlocked() = runTest {
        assertTrue(model.shouldBlock("+380501234567"))
    }

    @Test
    fun otherNumber_isAllowed() = runTest {
        assertFalse(model.shouldBlock("+380671234567"))
    }

    @Test
    fun emptyList_allowsEverything() = runTest {
        val empty = BlockerServiceModelImpl(FakeNumberStore(), MatcherImpl())
        assertFalse(empty.shouldBlock("+380501234567"))
    }

    @Test
    fun nullOrBlankNumber_isAllowed() = runTest {
        assertFalse(model.shouldBlock(null))
        assertFalse(model.shouldBlock("  "))
    }

    @Test
    fun storeFailure_failsOpen() = runTest {
        store.failLoad = true
        assertFalse(model.shouldBlock("+380501234567"))
    }

    @Test
    fun cancellation_isRethrown() = runTest {
        val cancelling = object : NumberStore {
            override suspend fun saveNumber(number: Number) = Unit
            override suspend fun deleteNumber(id: Number.Id) = Unit
            override fun loadNumbers(): Flow<List<Number>> = throw CancellationException("cancelled")
        }
        val result = runCatching { BlockerServiceModelImpl(cancelling, MatcherImpl()).shouldBlock("1") }
        assertTrue(result.exceptionOrNull() is CancellationException)
    }
}
