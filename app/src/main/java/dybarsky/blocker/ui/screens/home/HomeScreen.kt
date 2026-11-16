package dybarsky.blocker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
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
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.ui.common.Callback
import dybarsky.blocker.ui.common.LoadingIndicator
import dybarsky.blocker.ui.screens.home.HomeIntention.AddNumber
import dybarsky.blocker.ui.screens.home.HomeIntention.DeleteNumber
import dybarsky.blocker.ui.screens.home.HomeIntention.DismissError
import dybarsky.blocker.ui.screens.home.HomeIntention.EditNumber
import dybarsky.blocker.ui.screens.home.HomeIntention.OpenLog
import dybarsky.blocker.ui.screens.home.HomeIntention.Retry

@Composable
fun HomeScreen(
    goToEditor: (id: Number.Id?) -> Unit,
    goToLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val factory = BlockerApp.instance.injection.viewModelFactory
    val model = viewModel<HomeViewModel>(factory = factory)
    val state = model.stateFlow.collectAsStateWithLifecycle()
    val callback = Callback<HomeIntention> { intention ->
        when (intention) {
            is DeleteNumber -> model.deleteNumber(intention.id)
            is EditNumber -> goToEditor(intention.id)
            is AddNumber -> goToEditor(null)
            is Retry -> model.retry()
            is OpenLog -> goToLog()
            is DismissError -> model.dismissError()
        }
    }
    HomeScreen(state.value, callback, modifier)
}

@Composable
fun HomeScreen(
    state: HomeState,
    callback: Callback<HomeIntention>,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is HomeState.Loading -> HomeScreenLoading(modifier)
        is HomeState.Error -> HomeScreenError(callback, modifier)
        is HomeState.Content -> HomeScreenContent(
            data = state.data,
            deleteFailed = state.deleteFailed,
            callback = callback,
            modifier = modifier,
        )
    }
}

@Composable
fun HomeScreenError(
    callback: Callback<HomeIntention>,
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
                text = stringResource(R.string.error_load),
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
fun HomeScreenLoading(modifier: Modifier = Modifier) {
    LoadingIndicator(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    )
}

@Composable
fun HomeScreenContent(
    data: List<Number>,
    deleteFailed: Boolean,
    callback: Callback<HomeIntention>,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteErrorText = stringResource(R.string.error_delete)
    LaunchedEffect(deleteFailed) {
        if (deleteFailed) {
            snackbarHostState.showSnackbar(deleteErrorText)
            callback.submit(DismissError)
        }
    }
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 32.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 82.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceBright)
                .verticalScroll(rememberScrollState()),
        ) {
            data.forEach { NumberItem(it, deleteFailed, callback) }
        }
        TextButton(
            onClick = { callback.submit(OpenLog) },
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Text(stringResource(R.string.log_open))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 82.dp),
        )
        Fab(callback, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun NumberItem(
    number: Number,
    deleteFailed: Boolean,
    callback: Callback<HomeIntention>,
) {
    // Re-create the swipe state when a delete fails so the row snaps back.
    key(deleteFailed) {
    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(),
        enableDismissFromStartToEnd = false,
        onDismiss = { callback.submit(DeleteNumber(number.id)) },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceBright)
                    .clickable(onClick = { callback.submit(EditNumber(number.id)) })
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = number.mask,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "Видалити",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
    }
}

@Composable
fun Fab(
    callback: Callback<HomeIntention>,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = { callback.submit(AddNumber) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add"
            )
            Text(
                text = "Додати"
            )
        }
    }
}
