package dybarsky.blocker.ui.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dybarsky.blocker.BlockerApp
import dybarsky.blocker.R
import dybarsky.blocker.common.models.BlockedCall
import androidx.compose.foundation.lazy.items
import dybarsky.blocker.ui.common.ScreenCard
import dybarsky.blocker.ui.common.ScreenContainer
import dybarsky.blocker.ui.common.Callback
import dybarsky.blocker.ui.common.LoadingIndicator
import dybarsky.blocker.ui.screens.log.LogIntention.Clear
import dybarsky.blocker.ui.screens.log.LogIntention.DismissError
import dybarsky.blocker.ui.screens.log.LogIntention.Retry
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

@Composable
fun LogScreen(modifier: Modifier = Modifier) {
    val factory = BlockerApp.instance.injection.viewModelFactory
    val model = viewModel<LogViewModel>(factory = factory)
    val state = model.stateFlow.collectAsStateWithLifecycle()
    val callback = Callback<LogIntention> { intention ->
        when (intention) {
            is Clear -> model.clear()
            is Retry -> model.retry()
            is DismissError -> model.dismissError()
        }
    }
    LogScreen(state.value, callback, modifier)
}

@Composable
fun LogScreen(
    state: LogState,
    callback: Callback<LogIntention>,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is LogState.Loading -> ScreenContainer(modifier) {
            ScreenCard { LoadingIndicator(Modifier.fillMaxSize()) }
        }
        is LogState.Error -> LogScreenError(callback, modifier)
        is LogState.Content -> LogScreenContent(
            state = state,
            callback = callback,
            modifier = modifier,
        )
    }
}

@Composable
fun LogScreenError(
    callback: Callback<LogIntention>,
    modifier: Modifier = Modifier,
) {
    ScreenContainer(modifier) {
        ScreenCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.error_log_load),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = { callback.submit(Retry) }) {
                    Text(stringResource(R.string.error_retry))
                }
            }
        }
    }
}

@Composable
fun LogScreenContent(
    state: LogState.Content,
    callback: Callback<LogIntention>,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clearErrorText = stringResource(R.string.error_clear_log)
    LaunchedEffect(state.clearFailed) {
        if (state.clearFailed) {
            snackbarHostState.showSnackbar(clearErrorText)
            callback.submit(DismissError)
        }
    }
    ScreenContainer(modifier) {
        ScreenCard {
            if (state.items.isEmpty()) {
                Text(
                    text = stringResource(R.string.log_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                )
            } else {
                val days = remember(state.items) { state.items.groupByDay() }
                val today = remember(state.items) { LocalDate.now() }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    days.forEach { day ->
                        stickyHeader(key = day.date.toString()) { DayHeader(day.date, today) }
                        items(day.calls) { call -> LogItem(call) }
                        item(key = "gap-${day.date}") { Spacer(Modifier.height(DayGroupGap)) }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
        )
    }
}

/** Extra space after the last call of a day, to visually separate day groups. */
private val DayGroupGap = 16.dp

@Composable
fun DayHeader(date: LocalDate, today: LocalDate, modifier: Modifier = Modifier) {
    val text = when (relativeDay(date, today)) {
        RelativeDay.Today -> stringResource(R.string.log_today)
        RelativeDay.Yesterday -> stringResource(R.string.log_yesterday)
        RelativeDay.Other -> remember(date) {
            date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceBright)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
fun LogItem(call: BlockedCall, modifier: Modifier = Modifier) {
    val time = remember(call.timestamp) {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(call.timestamp))
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = call.number,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(R.string.log_mask, call.mask),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
