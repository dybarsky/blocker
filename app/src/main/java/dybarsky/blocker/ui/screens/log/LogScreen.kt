package dybarsky.blocker.ui.screens.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import dybarsky.blocker.ui.common.CardFab
import dybarsky.blocker.ui.common.CardFabClearance
import dybarsky.blocker.ui.common.ScreenCard
import dybarsky.blocker.ui.common.ScreenContainer
import dybarsky.blocker.ui.common.Callback
import dybarsky.blocker.ui.common.LoadingIndicator
import dybarsky.blocker.ui.screens.log.LogIntention.Clear
import dybarsky.blocker.ui.screens.log.LogIntention.DismissError
import dybarsky.blocker.ui.screens.log.LogIntention.Retry
import java.text.DateFormat
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
    var confirmClear by remember { mutableStateOf(false) }
    when (state) {
        is LogState.Loading -> ScreenContainer(modifier) {
            ScreenCard { LoadingIndicator(Modifier.fillMaxSize()) }
        }
        is LogState.Error -> LogScreenError(callback, modifier)
        is LogState.Content -> LogScreenContent(
            state = state,
            callback = callback,
            onClearClick = { confirmClear = true },
            modifier = modifier,
        )
    }
    if (confirmClear) {
        ClearLogDialog(
            onConfirm = {
                confirmClear = false
                callback.submit(Clear)
            },
            onDismiss = { confirmClear = false },
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
    onClearClick: () -> Unit,
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = CardFabClearance),
                ) {
                    items(state.items) { call -> LogItem(call) }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = CardFabClearance),
        )
        if (state.items.isNotEmpty()) {
            CardFab(
                icon = Icons.Default.Delete,
                contentDescription = stringResource(R.string.log_clear),
                onClick = onClearClick,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

@Composable
fun ClearLogDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.log_clear_title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.log_clear)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
fun LogItem(call: BlockedCall, modifier: Modifier = Modifier) {
    val time = remember(call.timestamp) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(call.timestamp))
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Text(text = call.number, style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(R.string.log_mask, call.mask),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
