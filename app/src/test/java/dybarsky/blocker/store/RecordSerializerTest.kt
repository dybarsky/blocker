package dybarsky.blocker.store

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class RecordSerializerTest {

    @Test
    fun roundTrip() = runTest {
        val records = listOf(Record(1, "+380*"), Record(2, "123"))
        val out = ByteArrayOutputStream()
        RecordSerializer.writeTo(records, out)
        val read = RecordSerializer.readFrom(ByteArrayInputStream(out.toByteArray()))
        assertEquals(records, read)
    }

    @Test
    fun invalidJson_throwsCorruptionException() {
        assertThrows(CorruptionException::class.java) {
            kotlinx.coroutines.runBlocking {
                RecordSerializer.readFrom(ByteArrayInputStream("not json".toByteArray()))
            }
        }
    }

    @Test
    fun defaultValue_isEmpty() = assertEquals(emptyList<Record>(), RecordSerializer.defaultValue)
}
