package dybarsky.blocker.ui

import dybarsky.blocker.testing.call
import dybarsky.blocker.ui.screens.log.RelativeDay
import dybarsky.blocker.ui.screens.log.groupByDay
import dybarsky.blocker.ui.screens.log.relativeDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class LogGroupingTest {

    private val zone = ZoneId.of("Europe/Berlin")

    private fun at(date: String, time: String): Long =
        LocalDateTime.parse("${date}T$time").atZone(zone).toInstant().toEpochMilli()

    @Test
    fun emptyList_hasNoGroups() = assertTrue(emptyList<dybarsky.blocker.common.models.BlockedCall>().groupByDay(zone).isEmpty())

    @Test
    fun sameDay_isOneGroup_keepingOrder() {
        val calls = listOf(call("b", timestamp = at("2026-11-16", "18:00")), call("a", timestamp = at("2026-11-16", "09:00")))
        val days = calls.groupByDay(zone)
        assertEquals(1, days.size)
        assertEquals(LocalDate.of(2026, 11, 16), days[0].date)
        assertEquals(listOf("b", "a"), days[0].calls.map { it.number })
    }

    @Test
    fun differentDays_areSeparateGroups_newestFirst() {
        val calls = listOf(
            call("3", timestamp = at("2026-11-16", "10:00")),
            call("2", timestamp = at("2026-11-15", "23:30")),
            call("1", timestamp = at("2026-11-15", "08:00")),
            call("0", timestamp = at("2026-11-01", "12:00")),
        )
        val days = calls.groupByDay(zone)
        assertEquals(
            listOf(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 1)),
            days.map { it.date },
        )
        assertEquals(listOf(listOf("3"), listOf("2", "1"), listOf("0")), days.map { d -> d.calls.map { it.number } })
    }

    @Test
    fun midnightBoundary_usesLocalZone() {
        // 23:59 and 00:01 local are different days, although only two minutes apart.
        val calls = listOf(call("b", timestamp = at("2026-11-16", "00:01")), call("a", timestamp = at("2026-11-15", "23:59")))
        assertEquals(2, calls.groupByDay(zone).size)
    }

    @Test
    fun sameInstant_inDifferentZones_canFallOnDifferentDays() {
        val ts = at("2026-11-16", "00:30") // 23:30 on the 15th in UTC
        val berlin = listOf(call("x", timestamp = ts)).groupByDay(zone).single().date
        val utc = listOf(call("x", timestamp = ts)).groupByDay(ZoneId.of("UTC")).single().date
        assertEquals(LocalDate.of(2026, 11, 16), berlin)
        assertEquals(LocalDate.of(2026, 11, 15), utc)
    }

    @Test
    fun relativeDay_labels() {
        val today = LocalDate.of(2026, 11, 16)
        assertEquals(RelativeDay.Today, relativeDay(today, today))
        assertEquals(RelativeDay.Yesterday, relativeDay(today.minusDays(1), today))
        assertEquals(RelativeDay.Other, relativeDay(today.minusDays(2), today))
        assertEquals(RelativeDay.Other, relativeDay(today.plusDays(1), today))
    }

    @Test
    fun relativeDay_acrossMonthAndYearBoundary() {
        assertEquals(RelativeDay.Yesterday, relativeDay(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1)))
    }
}
