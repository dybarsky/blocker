package dybarsky.blocker.ui.screens.log

import dybarsky.blocker.common.models.BlockedCall
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Blocked calls that happened on one local calendar [date], in the original (newest first) order. */
data class LogDay(
    val date: LocalDate,
    val calls: List<BlockedCall>,
)

enum class RelativeDay { Today, Yesterday, Other }

/**
 * Groups [this] by local calendar day. The input is expected newest first (as the store returns it),
 * so the groups, and the calls inside each group, stay newest first.
 */
fun List<BlockedCall>.groupByDay(zone: ZoneId = ZoneId.systemDefault()): List<LogDay> =
    groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
        .map { (date, calls) -> LogDay(date, calls) }

fun relativeDay(date: LocalDate, today: LocalDate): RelativeDay = when (date) {
    today -> RelativeDay.Today
    today.minusDays(1) -> RelativeDay.Yesterday
    else -> RelativeDay.Other
}
