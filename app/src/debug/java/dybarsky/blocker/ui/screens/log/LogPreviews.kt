package dybarsky.blocker.ui.screens.log

import androidx.compose.runtime.Composable
import dybarsky.blocker.common.models.BlockedCall
import dybarsky.blocker.ui.common.Callback
import dybarsky.blocker.ui.common.PreviewFrame
import dybarsky.blocker.ui.common.ScreenPreview
import java.time.LocalDate
import java.time.ZoneId

private val noop = Callback<LogIntention> { }

/** LogScreen compares with the real current date, so sample times are built relative to today. */
private fun at(daysAgo: Long, hour: Int, minute: Int): Long =
    LocalDate.now().minusDays(daysAgo).atTime(hour, minute)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Newest first, as the store returns it: today, yesterday, a few days ago, long ago. */
private fun sampleCalls() = listOf(
    BlockedCall("+4930123456", "+49 30 *", at(0, 14, 32)),
    BlockedCall("+380501234567", "+38050*4567", at(0, 9, 5)),
    BlockedCall("+4314112345", "+43*", at(1, 22, 10)),
    BlockedCall("+441651234567", "+44165*", at(3, 18, 47)),
    BlockedCall("+491451234567", "+49 145 *", at(3, 8, 15)),
    BlockedCall("+380671234567", "*", at(40, 12, 0)),
)

@Composable
private fun Preview(state: LogState) = PreviewFrame { LogScreen(state, noop) }

@ScreenPreview
@Composable
private fun LogContentPreview() = Preview(LogState.Content(sampleCalls()))

@ScreenPreview
@Composable
private fun LogEmptyPreview() = Preview(LogState.Content(emptyList()))

@ScreenPreview
@Composable
private fun LogLoadingPreview() = Preview(LogState.Loading)

@ScreenPreview
@Composable
private fun LogErrorPreview() = Preview(LogState.Error)
