package dybarsky.blocker.ui.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dybarsky.blocker.BlockerApp
import dybarsky.blocker.R
import dybarsky.blocker.common.models.BlockedCall
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
    when (state) {
        is LogState.Loading -> LoadingIndicator(modifier.fillMaxSize().systemBarsPadding())
        is LogState.Error -> LogScreenError(callback, modifier)
        is LogState.Content -> LogScreenContent(state, callback, modifier)
    }
}

@Composable
fun LogScreenError(
    callback: Callback<LogIntention>,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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
    var confirmClear by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 32.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.log_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { confirmClear = true },
                    enabled = state.items.isNotEmpty(),
                ) {
                    Text(stringResource(R.string.log_clear))
                }
            }
            if (state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.log_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceBright),
                ) {
                    itemsIndexed(state.items) { _, call -> LogItem(call) }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.log_clear_title)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    callback.submit(Clear)
                }) { Text(stringResource(R.string.log_clear)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
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
