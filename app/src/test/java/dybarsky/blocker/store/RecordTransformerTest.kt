package dybarsky.blocker.store

import dybarsky.blocker.common.models.Number
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordTransformerTest {

    @Test
    fun newNumber_getsGeneratedId() {
        val record = Number(Number.Id.New, "123").asRecord()
        assertNotEquals(Number.Id.New.value, record.id)
        assertTrue(record.id > 0)
        assertEquals("123", record.mask)
    }

    @Test
    fun existingId_isPreserved() {
        assertEquals(42L, Number(Number.Id(42), "123").asRecord().id)
    }

    @Test
    fun record_roundTripsToNumber() {
        val number = Number(Number.Id(7), "+38*")
        assertEquals(number, number.asRecord().asNumber())
    }
}
