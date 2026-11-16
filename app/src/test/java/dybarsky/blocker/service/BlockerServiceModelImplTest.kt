package dybarsky.blocker.service

import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.common.models.BlockedCall
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.domain.MatcherImpl
import dybarsky.blocker.testing.FakeCallLogStore
import dybarsky.blocker.testing.FakeNumberStore
import dybarsky.blocker.testing.number
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class BlockerServiceModelImplTest {

    private val store = FakeNumberStore(listOf(number(1, "+38050*"), number(2, "+43*")))
    private val callLog = FakeCallLogStore()
    private var time = 1_000L
    private val model = BlockerServiceModelImpl(store, callLog, MatcherImpl()) { time }

    // --- screen ----------------------------------------------------------------------------

    @Test
    fun matchingNumber_isBlockedWithMatchedMask() = runTest {
        assertEquals(Verdict.Block("+38050*"), model.screen("+380501234567"))
    }

    @Test
    fun otherNumber_isAllowed() = runTest {
        assertEquals(Verdict.Allow, model.screen("+380671234567"))
    }

    @Test
    fun firstMatchingMask_isReported() = runTest {
        val overlapping = FakeNumberStore(listOf(number(1, "+43*"), number(2, "+436*")))
        val m = BlockerServiceModelImpl(overlapping, callLog, MatcherImpl())
        assertEquals(Verdict.Block("+43*"), m.screen("+436641234567"))
    }

    @Test
    fun emptyList_allowsEverything() = runTest {
        val empty = BlockerServiceModelImpl(FakeNumberStore(), callLog, MatcherImpl())
        assertEquals(Verdict.Allow, empty.screen("+380501234567"))
    }

    @Test
    fun nullOrBlankNumber_isAllowed() = runTest {
        assertEquals(Verdict.Allow, model.screen(null))
        assertEquals(Verdict.Allow, model.screen("  "))
    }

    @Test
    fun storeFailure_failsOpen() = runTest {
        store.failLoad = true
        assertEquals(Verdict.Allow, model.screen("+380501234567"))
    }

    @Test
    fun cancellation_isRethrown() = runTest {
        val cancelling = object : NumberStore {
            override suspend fun saveNumber(number: Number) = Unit
            override suspend fun deleteNumber(id: Number.Id) = Unit
            override fun loadNumbers(): Flow<List<Number>> = throw CancellationException("cancelled")
        }
        val result = runCatching { BlockerServiceModelImpl(cancelling, callLog, MatcherImpl()).screen("1") }
        assertTrue(result.exceptionOrNull() is CancellationException)
    }

    // --- record ----------------------------------------------------------------------------

    @Test
    fun blockedCall_isRecordedWithNumberMaskAndTime() = runTest {
        model.record("+380501234567", Verdict.Block("+38050*"))
        assertEquals(listOf(BlockedCall("+380501234567", "+38050*", 1_000L)), callLog.calls.value)
    }

    @Test
    fun newestCall_isFirst() = runTest {
        model.record("+4311", Verdict.Block("+43*"))
        time = 2_000L
        model.record("+4322", Verdict.Block("+43*"))
        assertEquals(listOf(2_000L, 1_000L), callLog.calls.value.map { it.timestamp })
    }

    @Test
    fun allowedCall_isNotRecorded() = runTest {
        model.record("+380671234567", Verdict.Allow)
        assertTrue(callLog.calls.value.isEmpty())
    }

    @Test
    fun blankNumber_isNotRecorded() = runTest {
        model.record(null, Verdict.Block("*"))
        model.record(" ", Verdict.Block("*"))
        assertTrue(callLog.calls.value.isEmpty())
    }

    @Test
    fun loggingFailure_doesNotThrow() = runTest {
        callLog.failAdd = true
        model.record("+380501234567", Verdict.Block("+38050*"))
        assertTrue(callLog.calls.value.isEmpty())
    }

    @Test
    fun loggingFailure_doesNotChangeVerdict() = runTest {
        callLog.failAdd = true
        val verdict = model.screen("+380501234567")
        model.record("+380501234567", verdict)
        assertEquals(Verdict.Block("+38050*"), verdict)
    }
}
