package dybarsky.blocker.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatcherImplTest {

    private val matcher = MatcherImpl()

    private fun assertMatches(mask: String, number: String) =
        assertTrue("'$mask' should match '$number'", matcher(mask, number))

    private fun assertNoMatch(mask: String, number: String) =
        assertFalse("'$mask' should NOT match '$number'", matcher(mask, number))

    // --- Country prefix: +43* ---------------------------------------------------------------

    @Test
    fun countryPrefix_matchesAnyNumberOfThatCountry() {
        assertMatches("+43*", "+436641234567")
        assertMatches("+43*", "+4311234567")
    }

    @Test
    fun countryPrefix_doesNotMatchOtherCountries() {
        assertNoMatch("+43*", "+4915112345678")
        assertNoMatch("+43*", "+380501234567")
        assertNoMatch("+43*", "436641234567") // no plus sign
    }

    @Test
    fun countryPrefix_wildcardMatchesEmptyTail() = assertMatches("+43*", "+43")

    // --- Longer prefix: +44165* ---------------------------------------------------------------

    @Test
    fun longPrefix_matchesOnlyThatPrefix() {
        assertMatches("+44165*", "+441651234567")
        assertNoMatch("+44165*", "+441661234567")
        assertNoMatch("+44165*", "+441601234567")
        assertNoMatch("+44165*", "+4416") // shorter than the prefix
    }

    // --- Prefix with spaces: +49 145 * ---------------------------------------------------------

    @Test
    fun spacesInMask_areIgnored() {
        assertMatches("+49 145 *", "+491451234567")
        assertNoMatch("+49 145 *", "+491461234567")
        assertNoMatch("+49 145 *", "+4915112345678")
    }

    @Test
    fun spacesInNumber_areIgnored() {
        assertMatches("+49 145 *", "+49 145 1234567")
        assertMatches("+491451234567", "+49 145 123 45 67")
    }

    @Test
    fun spacesPlacedDifferentlyInMaskAndNumber_stillMatch() {
        assertMatches("+4 9145*", "+49 1 45 99")
    }

    // --- Exact masks ---------------------------------------------------------------------------

    @Test
    fun maskWithoutWildcard_requiresExactMatch() {
        assertMatches("+380501234567", "+380501234567")
        assertNoMatch("+380501234567", "+380501234568")
    }

    @Test
    fun maskWithoutWildcard_isNotAPrefix() {
        assertNoMatch("+43", "+436641234567")
        assertNoMatch("+436641234567", "+43664123456")
    }

    // --- Wildcard positions --------------------------------------------------------------------

    @Test
    fun wildcardInTheMiddle() {
        assertMatches("+38050*4567", "+380501234567")
        assertMatches("+38050*4567", "+380504567") // empty middle
        assertNoMatch("+38050*4567", "+380501234568")
    }

    @Test
    fun wildcardAtTheStart() {
        assertMatches("*4567", "+380501234567")
        assertNoMatch("*4567", "+380501234568")
    }

    @Test
    fun onlyWildcard_matchesAnyNonEmptyNumber() {
        assertMatches("*", "+380501234567")
        assertMatches("*", "112")
    }

    @Test
    fun repeatedWildcards_behaveLikeOne() {
        assertMatches("+38050***4567", "+380501234567")
        assertMatches("+43**", "+436641234567")
    }

    @Test
    fun multipleWildcards_needBacktracking() {
        assertMatches("*ab*cd", "xabyabcd")
        assertMatches("*1*2*3", "919293")
        assertNoMatch("*1*2*3", "919294")
        assertNoMatch("*1*2*3", "9192939") // must end with 3
    }

    // --- Formatting characters -----------------------------------------------------------------

    @Test
    fun dashesParenthesesAndDots_areIgnored() {
        assertMatches("+49 (145) *", "+49145123456")
        assertMatches("+49-145-*", "+491451234567")
        assertMatches("+49.145.*", "+491451234567")
        assertMatches("+49145123456", "+49 (145) 123-456")
    }

    @Test
    fun surroundingWhitespace_isIgnored() {
        assertMatches("  +43*  ", "+436641234567")
        assertMatches("+43*", " +436641234567 ")
    }

    // --- International prefix: 00 == + ---------------------------------------------------------

    @Test
    fun doubleZeroMask_matchesPlusNumber() {
        assertMatches("0049 145 *", "+49145123")
        assertMatches("0049145123", "+49145123")
        assertNoMatch("0049 145 *", "+49151123")
    }

    @Test
    fun plusMask_matchesDoubleZeroNumber() {
        assertMatches("+49145*", "0049145123")
        assertMatches("+49 145 *", "00 49 145 123")
        assertNoMatch("+49145*", "0049151123")
    }

    @Test
    fun doubleZeroMask_matchesDoubleZeroNumber() {
        assertMatches("0049145*", "0049145123")
        assertNoMatch("0049145*", "0049151123")
    }

    @Test
    fun doubleZeroWithSeparators() {
        assertMatches("00 49 (145) *", "+49145123")
        assertMatches("00-49-(145)-*", "0049 145 123")
        assertMatches("(00) 49 *", "+4912")
    }

    @Test
    fun onlyLeadingDoubleZeroIsInternationalPrefix() {
        assertMatches("+4900*", "+49001")
        assertNoMatch("+4900*", "+49 1")
        assertMatches("+4900*", "004900") // number's leading 00 -> +4900
        assertNoMatch("+49*", "+0049") // +0049 is not 0049...
    }

    @Test
    fun doubleZeroInTheMiddle_isLiteral() {
        assertMatches("+1*00*", "+1 500 12")
        assertNoMatch("+1*00*", "+1 5 12")
        assertNoMatch("1*00", "+100") // no leading 00, no plus
    }

    @Test
    fun nationalFormat_isNotMappedToInternational() {
        assertNoMatch("0151*", "+49151123")
        assertNoMatch("+49151*", "0151123")
        assertMatches("0151*", "0151123")
    }

    @Test
    fun singleLeadingZero_isLiteral() {
        assertMatches("0*", "0151123")
        assertNoMatch("0*", "+49151123")
        assertNoMatch("0", "00")
        assertMatches("0", "0")
    }

    @Test
    fun doubleZeroAlone_isJustPlus() {
        assertMatches("00", "+")
        assertMatches("+", "00")
        assertMatches("00", "00")
        assertNoMatch("00", "+49145123")
        assertNoMatch("00", "0")
        assertNoMatch("00", "000")
    }

    @Test
    fun doubleZeroWildcard_matchesAllInternationalNumbers() {
        assertMatches("00*", "+49145123")
        assertMatches("00*", "0049145123")
        assertNoMatch("00*", "0151123")
        assertNoMatch("00*", "112")
    }

    @Test
    fun tripleZero_keepsTheThirdZero() {
        assertMatches("000*", "+0123")
        assertNoMatch("000*", "+123")
    }

    // --- Edge cases ----------------------------------------------------------------------------

    @Test
    fun plusIsLiteral_notARegexQuantifier() {
        assertNoMatch("+43*", "443")
        assertNoMatch("+", "5")
        assertMatches("+", "+")
    }

    @Test
    fun emptyOrBlankMask_neverMatches() {
        assertNoMatch("", "+436641234567")
        assertNoMatch("   ", "+436641234567")
        assertNoMatch("-()", "+436641234567")
    }

    @Test
    fun emptyOrBlankNumber_neverMatches() {
        assertNoMatch("*", "")
        assertNoMatch("+43*", "   ")
    }

    @Test
    fun veryLongInputs_doNotBlowUp() {
        val number = "1".repeat(5_000)
        assertNoMatch("*1*1*1*1*2", number)
        assertMatches("*1*1*1*1", number)
    }
}
