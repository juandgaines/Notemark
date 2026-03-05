package com.juandgaines.notemark.note.presentation.note_list

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juandgaines.notemark.R
import com.juandgaines.notemark.core.presentation.util.DeviceConfiguration
import com.juandgaines.notemark.core.presentation.util.ObserveAsEvents
import com.juandgaines.notemark.core.presentation.util.currentDeviceConfiguration
import com.juandgaines.notemark.note.presentation.components.DeleteNoteDialog
import com.juandgaines.notemark.note.presentation.components.GradientFab
import com.juandgaines.notemark.note.presentation.components.NoteCard
import com.juandgaines.notemark.note.presentation.components.NoteListTopBar
import com.juandgaines.notemark.ui.theme.Surface
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteListScreenRoot(
    viewModel: NoteListViewModel = koinViewModel(),
    onNavigateToNote: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NoteListEvent.NavigateToNewNote -> onNavigateToNote(event.noteId)
            is NoteListEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
            }
            is NoteListEvent.NoteDeleted -> {
                Toast.makeText(context, context.getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
            }
        }
    }

    NoteListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is NoteListAction.OnNoteClick -> onNavigateToNote(action.noteId)
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun NoteListScreen(
    state: NoteListState,
    onAction: (NoteListAction) -> Unit,
) {
    val deviceConfig = currentDeviceConfiguration()
    val columns = when (deviceConfig) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 2
        else -> 3
    }
    val contentPreviewMaxChars = if (deviceConfig.isMobile) 150 else 250

    Scaffold(
        containerColor = Surface,
        topBar = {
            NoteListTopBar(profileInitials = state.profileInitials)
        },
        floatingActionButton = {
            GradientFab(onClick = { onAction(NoteListAction.OnCreateNoteClick) })
        },
    ) { padding ->
        if (state.notes.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.empty_notes_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
            ) {
                items(state.notes, key = { it.id }) { noteUi ->
                    NoteCard(
                        noteUi = noteUi,
                        contentPreviewMaxChars = contentPreviewMaxChars,
                        onClick = { onAction(NoteListAction.OnNoteClick(noteUi.id)) },
                        onLongClick = { onAction(NoteListAction.OnNoteLongPress(noteUi.id)) },
                    )
                }
            }
        }
    }

    if (state.showDeleteDialog) {
        DeleteNoteDialog(
            onConfirm = { onAction(NoteListAction.OnConfirmDelete) },
            onDismiss = { onAction(NoteListAction.OnDismissDeleteDialog) },
        )
    }
}
