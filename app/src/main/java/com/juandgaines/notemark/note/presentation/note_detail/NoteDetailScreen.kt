package com.juandgaines.notemark.note.presentation.note_detail

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import com.juandgaines.notemark.core.presentation.util.DeviceConfiguration
import com.juandgaines.notemark.core.presentation.util.ObserveAsEvents
import com.juandgaines.notemark.core.presentation.util.currentDeviceConfiguration
import com.juandgaines.notemark.note.presentation.components.DiscardChangesDialog
import com.juandgaines.notemark.note.presentation.components.NoteDetailExtendedFab
import com.juandgaines.notemark.note.presentation.components.NoteDetailTopBar
import com.juandgaines.notemark.note.presentation.components.NoteDetailViewTopBar
import com.juandgaines.notemark.note.presentation.components.NoteMetadata
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurfaceVariant
import com.juandgaines.notemark.ui.theme.SurfaceLowest
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteDetailScreenRoot(
    viewModel: NoteDetailViewModel = koinViewModel(),
    onClose: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val deviceConfig = currentDeviceConfiguration()

    BackHandler(enabled = true) {
        when (state.mode) {
            NoteDetailMode.VIEW -> viewModel.onAction(NoteDetailAction.OnBackClick)
            NoteDetailMode.EDIT -> viewModel.onAction(NoteDetailAction.OnCloseEditMode)
            NoteDetailMode.READER -> viewModel.onAction(NoteDetailAction.OnExitReaderMode)
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NoteDetailEvent.NoteSaved -> {
                Toast.makeText(context, context.getString(R.string.note_saved), Toast.LENGTH_SHORT).show()
            }
            is NoteDetailEvent.CloseScreen -> onClose()
            is NoteDetailEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
            }
            is NoteDetailEvent.LockLandscape -> {
                if (deviceConfig.isMobile) {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
            is NoteDetailEvent.UnlockOrientation -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    NoteDetailScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun NoteDetailScreen(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    val deviceConfig = currentDeviceConfiguration()
    val isLandscape = deviceConfig == DeviceConfiguration.MOBILE_LANDSCAPE ||
            deviceConfig == DeviceConfiguration.TABLET_LANDSCAPE ||
            deviceConfig == DeviceConfiguration.DESKTOP

    when (state.mode) {
        NoteDetailMode.VIEW -> ViewModeContent(state, onAction, isLandscape)
        NoteDetailMode.EDIT -> EditModeContent(state, onAction)
        NoteDetailMode.READER -> ReaderModeContent(state, onAction)
    }

    if (state.showDiscardDialog) {
        DiscardChangesDialog(
            onConfirmDiscard = { onAction(NoteDetailAction.OnConfirmDiscard) },
            onKeepEditing = { onAction(NoteDetailAction.OnDismissDiscardDialog) },
        )
    }
}

@Composable
private fun ViewModeContent(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
    isLandscape: Boolean,
) {
    if (isLandscape) {
        LandscapeViewModeContent(state, onAction)
    } else {
        PortraitViewModeContent(state, onAction)
    }
}

@Composable
private fun PortraitViewModeContent(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    Scaffold(
        containerColor = SurfaceLowest,
        topBar = {
            NoteDetailViewTopBar(
                onBackClick = { onAction(NoteDetailAction.OnBackClick) },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.titleTextState.text.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))
                NoteMetadata(
                    createdAt = state.createdAt,
                    lastEditedAt = state.lastEditedAt,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.contentTextState.text.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface,
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
            NoteDetailExtendedFab(
                currentMode = NoteDetailMode.VIEW,
                onEditClick = { onAction(NoteDetailAction.OnEditClick) },
                onReaderClick = { onAction(NoteDetailAction.OnReaderClick) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun LandscapeViewModeContent(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    Scaffold(
        containerColor = SurfaceLowest,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Left side: back navigation
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 4.dp, top = 8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    IconButton(onClick = { onAction(NoteDetailAction.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.all_notes),
                        )
                    }
                    Text(
                        text = stringResource(R.string.all_notes).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                // Right side: content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.titleTextState.text.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    NoteMetadata(
                        createdAt = state.createdAt,
                        lastEditedAt = state.lastEditedAt,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.contentTextState.text.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface,
                    )
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            NoteDetailExtendedFab(
                currentMode = NoteDetailMode.VIEW,
                onEditClick = { onAction(NoteDetailAction.OnEditClick) },
                onReaderClick = { onAction(NoteDetailAction.OnReaderClick) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun EditModeContent(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        containerColor = SurfaceLowest,
        topBar = {
            NoteDetailTopBar(
                onCloseClick = { onAction(NoteDetailAction.OnCloseEditMode) },
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
}

@Composable
private fun ReaderModeContent(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    val scrollState = rememberScrollState()

    // Auto-hide UI after 5 seconds
    if (state.isUiVisible) {
        LaunchedEffect(state.isUiVisible) {
            delay(5_000L)
            onAction(NoteDetailAction.OnScrollStart)
        }
    }

    // Hide UI on scroll
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .collect { isScrolling ->
                if (isScrolling) {
                    onAction(NoteDetailAction.OnScrollStart)
                }
            }
    }

    Scaffold(
        containerColor = SurfaceLowest,
        topBar = {
            AnimatedVisibility(
                visible = state.isUiVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                NoteDetailViewTopBar(
                    onBackClick = { onAction(NoteDetailAction.OnExitReaderMode) },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    onAction(NoteDetailAction.OnScreenTap)
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.titleTextState.text.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))
                NoteMetadata(
                    createdAt = state.createdAt,
                    lastEditedAt = state.lastEditedAt,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.contentTextState.text.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface,
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
            AnimatedVisibility(
                visible = state.isUiVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            ) {
                NoteDetailExtendedFab(
                    currentMode = NoteDetailMode.READER,
                    onEditClick = { onAction(NoteDetailAction.OnEditClick) },
                    onReaderClick = { onAction(NoteDetailAction.OnExitReaderMode) },
                )
            }
        }
    }
}
