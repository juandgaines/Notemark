package com.juandgaines.notemark.note.presentation.note_detail

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juandgaines.notemark.R
import com.juandgaines.notemark.core.presentation.util.DeviceConfiguration
import com.juandgaines.notemark.core.presentation.util.ObserveAsEvents
import com.juandgaines.notemark.core.presentation.util.currentDeviceConfiguration
import com.juandgaines.notemark.note.presentation.components.NoteDetailModeFab
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
    val activity = LocalActivity.current
    val deviceConfig = currentDeviceConfiguration()

    BackHandler(enabled = true) {
        when (state.mode) {
            NoteDetailMode.EDIT -> viewModel.onAction(NoteDetailAction.OnCloseClick)
            NoteDetailMode.VIEW, NoteDetailMode.READER -> viewModel.onAction(NoteDetailAction.OnBackClick)
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NoteDetailEvent.CloseScreen -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                onClose()
            }
            is NoteDetailEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
            }
            is NoteDetailEvent.EnterReaderMode -> {
                if (deviceConfig.isMobile) {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
            is NoteDetailEvent.ExitReaderMode -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
    when (state.mode) {
        NoteDetailMode.VIEW -> ViewModeScreen(state = state, onAction = onAction)
        NoteDetailMode.EDIT -> EditModeScreen(state = state, onAction = onAction)
        NoteDetailMode.READER -> ReaderModeScreen(state = state, onAction = onAction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewModeScreen(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    val deviceConfig = currentDeviceConfiguration()
    val isLandscape = deviceConfig == DeviceConfiguration.MOBILE_LANDSCAPE ||
            deviceConfig == DeviceConfiguration.TABLET_LANDSCAPE ||
            deviceConfig == DeviceConfiguration.DESKTOP

    Scaffold(
        containerColor = SurfaceLowest,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .clickable { onAction(NoteDetailAction.OnBackClick) }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.all_notes),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLowest,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (isLandscape) {
                LandscapeViewContent(state = state)
            } else {
                PortraitViewContent(state = state)
            }

            NoteDetailModeFab(
                currentMode = state.mode,
                isVisible = true,
                onEditClick = { onAction(NoteDetailAction.OnSwitchToEditMode) },
                onReaderClick = { onAction(NoteDetailAction.OnSwitchToReaderMode) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun PortraitViewContent(
    state: NoteDetailState,
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
        MetadataRow(
            createdAt = state.createdAt,
            lastEditedAt = state.lastEditedAt,
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.contentTextState.text.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface,
        )
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun LandscapeViewContent(
    state: NoteDetailState,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
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
            MetadataRow(
                createdAt = state.createdAt,
                lastEditedAt = state.lastEditedAt,
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.contentTextState.text.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface,
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MetadataRow(
    createdAt: String,
    lastEditedAt: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(R.string.date_created),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
            )
            Text(
                text = createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface,
            )
        }
        VerticalDivider(modifier = Modifier.fillMaxHeight())
        Column {
            Text(
                text = stringResource(R.string.last_edited),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
            )
            Text(
                text = lastEditedAt,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModeScreen(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderModeScreen(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (available.y != 0f) {
                    onAction(NoteDetailAction.OnReaderScroll)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    Scaffold(
        containerColor = SurfaceLowest,
        topBar = {
            AnimatedVisibility(
                visible = state.areUiElementsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        Row(
                            modifier = Modifier
                                .clickable { onAction(NoteDetailAction.OnBackClick) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.all_notes),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceLowest,
                    ),
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures {
                        onAction(NoteDetailAction.OnReaderTap)
                    }
                }
                .nestedScroll(nestedScrollConnection),
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
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.contentTextState.text.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface,
                )
                Spacer(modifier = Modifier.height(80.dp))
            }

            NoteDetailModeFab(
                currentMode = state.mode,
                isVisible = state.areUiElementsVisible,
                onEditClick = { onAction(NoteDetailAction.OnSwitchToEditMode) },
                onReaderClick = { onAction(NoteDetailAction.OnSwitchToViewMode) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
