package dybarsky.blocker.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatcherImplTest {

    private val matcher = MatcherImpl()

    @Test
    fun exactMatch() = assertTrue(matcher("+380501234567", "+380501234567"))

    @Test
    fun differentNumber_doesNotMatch() = assertFalse(matcher("+380501234567", "+380501234568"))

    @Test
    fun wildcard_matchesAnySingleCharacter() {
        assertTrue(matcher("+38050***4567", "+380501234567"))
        assertFalse(matcher("+38050***4567", "+380601234567"))
    }

    @Test
    fun shorterMask_actsAsPrefix() {
        // Documented quirk: zip() truncates to the shorter string.
        assertTrue(matcher("+38050", "+380501234567"))
    }
}
