package dybarsky.blocker.common

import dybarsky.blocker.common.util.runCatchingSuspend
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class RunCatchingSuspendTest {

    @Test
    fun success_isReturned() = runTest {
        val result = runCatchingSuspend { delay(1); 5 }
        assertEquals(5, result.getOrNull())
    }

    @Test
    fun failure_isCaptured() = runTest {
        val result = runCatchingSuspend { throw IOException("boom") }
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun cancellation_isRethrown() {
        assertThrows(CancellationException::class.java) {
            runCatchingSuspend { throw CancellationException("cancelled") }
        }
    }
}
