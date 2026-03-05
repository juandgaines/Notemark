package com.juandgaines.notemark.note.presentation.note_detail

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juandgaines.notemark.R
import com.juandgaines.notemark.core.presentation.util.ObserveAsEvents
import com.juandgaines.notemark.note.presentation.components.DiscardChangesDialog
import com.juandgaines.notemark.note.presentation.components.NoteDetailTopBar
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurfaceVariant
import com.juandgaines.notemark.ui.theme.SurfaceLowest
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteDetailScreenRoot(
    viewModel: NoteDetailViewModel = koinViewModel(),
    onClose: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler(enabled = true) {
        viewModel.onAction(NoteDetailAction.OnCloseClick)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NoteDetailEvent.NoteSaved -> {
                Toast.makeText(context, context.getString(R.string.note_saved), Toast.LENGTH_SHORT).show()
                onClose()
            }
            is NoteDetailEvent.CloseScreen -> onClose()
            is NoteDetailEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
            }
        }
    }

    NoteDetailScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun NoteDetailScreen(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.noteId) {
        if (state.noteId.isNotEmpty() && state.titleTextState.text.isEmpty() && state.contentTextState.text.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        containerColor = SurfaceLowest,
        topBar = {
            NoteDetailTopBar(
                onCloseClick = { onAction(NoteDetailAction.OnCloseClick) },
                onSaveClick = { onAction(NoteDetailAction.OnSaveClick) },
                canSave = state.canSave,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            BasicTextField(
                state = state.titleTextState,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.headlineLarge.copy(color = OnSurface),
                cursorBrush = SolidColor(OnSurface),
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (state.titleTextState.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.note_title_placeholder),
                                style = MaterialTheme.typography.headlineLarge,
                                color = OnSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            BasicTextField(
                state = state.contentTextState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
                cursorBrush = SolidColor(OnSurface),
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        if (state.contentTextState.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.note_content_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }

    if (state.showDiscardDialog) {
        DiscardChangesDialog(
            onConfirmDiscard = { onAction(NoteDetailAction.OnConfirmDiscard) },
            onKeepEditing = { onAction(NoteDetailAction.OnDismissDiscardDialog) },
        )
    }
}
