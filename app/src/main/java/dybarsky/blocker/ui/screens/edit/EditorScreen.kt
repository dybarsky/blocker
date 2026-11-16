package dybarsky.blocker.ui.screens.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType.Companion.Phone
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dybarsky.blocker.BlockerApp
import dybarsky.blocker.R
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.ui.common.Callback
import dybarsky.blocker.ui.common.LoadingIndicator
import dybarsky.blocker.ui.screens.edit.EditorIntention.Close
import dybarsky.blocker.ui.screens.edit.EditorIntention.Save

@Composable
fun EditorScreen(
    id: Number.Id?,
    close: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val factory = BlockerApp.instance.injection.viewModelFactory
    val extras = EditorViewModel.extras(id)
    val model = viewModel<EditorViewModel>(factory = factory, extras = extras)
    val state = model.stateFlow.collectAsStateWithLifecycle()
    val callback = Callback<EditorIntention> { intention ->
        when (intention) {
            is Close -> close()
            is Save  -> model.saveNumber(intention.mask)
        }
    }
    EditorScreen(state.value, callback, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: EditorState,
    callback: Callback<EditorIntention>,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = { /* no-op */ },
        modifier = modifier,
    ) {
        Surface(
            modifier = modifier,
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            when (state) {
                is EditorState.Loading -> LoadingIndicator(
                    modifier = modifier
                        .defaultMinSize(minWidth = 280.dp, minHeight = 160.dp)
                        .padding(24.dp),
                )

                is EditorState.Error -> EditorScreenError(
                    callback = callback,
                    modifier = modifier,
                )

                is EditorState.Content -> EditorScreenContent(
                    mask = state.mask,
                    saveFailed = state.saveFailed,
                    callback = callback,
                    modifier = modifier,
                )

                is EditorState.Saved -> {
                    LaunchedEffect(Unit) { callback.submit(Close) }
                }
            }
        }
    }
}

@Composable
fun EditorScreenError(
    callback: Callback<EditorIntention>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(24.dp)) {
        Box(Modifier.padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.error_editor_load),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        TextButton(
            onClick = { callback.submit(Close) },
            modifier = Modifier.align(Alignment.End),
            content = { Text(stringResource(R.string.action_close)) },
        )
    }
}

@Composable
fun EditorScreenContent(
    mask: String?,
    saveFailed: Boolean,
    callback: Callback<EditorIntention>,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val textFieldState = rememberTextFieldState(initialText = mask ?: "")
    LaunchedEffect(mask) { focusRequester.requestFocus() }
    Column(modifier = modifier.padding(24.dp)) {
        Box(Modifier.padding(bottom = 16.dp)) {
            Text(
                text = if (mask == null) "Додати номер" else "Редагувати номер",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Box(Modifier.padding(bottom = 24.dp)) {
            OutlinedTextField(
                state = textFieldState,
                keyboardOptions = KeyboardOptions(keyboardType = Phone),
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = MaterialTheme.typography.titleLarge,
                isError = saveFailed,
                supportingText = if (saveFailed) {
                    { Text(stringResource(R.string.error_save)) }
                } else null,
                modifier = Modifier.focusRequester(focusRequester),
            )
        }
        Row(modifier = Modifier.align(Alignment.End)) {
            TextButton(
                onClick = { callback.submit(Close)},
                content = { Text("Скасувати") },
            )
            TextButton(
                onClick = { callback.submit(Save(textFieldState.text.toString())) },
                content = { Text("Зберегти") },
            )
        }
    }
}
