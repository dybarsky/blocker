package dybarsky.blocker.store

import dybarsky.blocker.common.models.BlockedCall
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CallRecordTest {

    private val serializer = JsonListSerializer(CallRecord.serializer())

    @Test
    fun blockedCall_roundTripsThroughRecord() {
        val call = BlockedCall("+4366412", "+43*", 123L)
        assertEquals(call, call.asRecord().asBlockedCall())
    }

    @Test
    fun serializer_roundTrip() = runTest {
        val records = listOf(CallRecord("+4311", "+43*", 1), CallRecord("+4922", "*", 2))
        val out = ByteArrayOutputStream()
        serializer.writeTo(records, out)
        assertEquals(records, serializer.readFrom(ByteArrayInputStream(out.toByteArray())))
    }

    @Test
    fun serializer_invalidJson_throwsCorruptionException() {
        assertThrows(androidx.datastore.core.CorruptionException::class.java) {
            kotlinx.coroutines.runBlocking { serializer.readFrom(ByteArrayInputStream("{".toByteArray())) }
        }
    }

    @Test
    fun plusCapped_appendsBelowLimit() {
        assertEquals(listOf(1, 2, 3), listOf(1, 2).plusCapped(3, limit = 5))
    }

    @Test
    fun plusCapped_dropsOldestAtLimit() {
        assertEquals(listOf(2, 3, 4), listOf(1, 2, 3).plusCapped(4, limit = 3))
    }

    @Test
    fun plusCapped_toEmptyList() {
        assertEquals(listOf(1), emptyList<Int>().plusCapped(1, limit = 3))
    }

    @Test
    fun maxEntries_isTwoHundred() = assertEquals(200, CallLogStoreImpl.MAX_ENTRIES)
}
