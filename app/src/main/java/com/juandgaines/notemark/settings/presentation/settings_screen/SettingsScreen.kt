package com.juandgaines.notemark.settings.presentation.settings_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juandgaines.notemark.R
import com.juandgaines.notemark.core.presentation.util.ObserveAsEvents
import com.juandgaines.notemark.settings.domain.SyncInterval
import com.juandgaines.notemark.ui.theme.ErrorRed
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurfaceVariant
import com.juandgaines.notemark.ui.theme.Surface
import com.juandgaines.notemark.ui.theme.SurfaceLowest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreenRoot(
    viewModel: SettingsViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SettingsEvent.LogoutSuccess -> onLogoutSuccess()
            is SettingsEvent.Error -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
            }
        }
    }

    SettingsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is SettingsAction.OnBackClick -> onBackClick()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
) {
    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(SettingsAction.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Section header: DATA
            item {
                Text(
                    text = stringResource(R.string.settings_section_data),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.16.sp,
                    ),
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            // Sync Interval Row
            item {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction(SettingsAction.OnSyncIntervalClick) }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = OnSurface,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.sync_interval),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp,
                            ),
                            color = OnSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = state.syncInterval.displayName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                            color = OnSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                        )
                    }

                    if (state.showSyncIntervalDropdown) {
                        SyncIntervalDropdown(
                            selectedInterval = state.syncInterval,
                            onIntervalSelected = { onAction(SettingsAction.OnSyncIntervalSelected(it)) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 56.dp, end = 16.dp),
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    color = Surface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // Sync Data Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isSyncing) {
                            onAction(SettingsAction.OnSyncDataClick)
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = OnSurface,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.sync_data),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp,
                            ),
                            color = OnSurface,
                        )
                        Text(
                            text = stringResource(R.string.last_sync, state.lastSyncTimestamp.asString()),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                            color = OnSurfaceVariant,
                        )
                    }
                    if (state.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    color = Surface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section header: ACCOUNT
            item {
                Text(
                    text = stringResource(R.string.settings_section_account),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.16.sp,
                    ),
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            // Log out Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isLoggingOut) {
                            onAction(SettingsAction.OnLogoutClick)
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = ErrorRed,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.log_out),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.sp,
                        ),
                        color = ErrorRed,
                    )
                }
            }
        }
    }

    if (state.showUnsyncedDialog) {
        UnsyncedChangesDialog(
            onSyncNow = { onAction(SettingsAction.OnSyncNowClick) },
            onLogoutWithoutSyncing = { onAction(SettingsAction.OnLogoutWithoutSyncingClick) },
            onDismiss = { onAction(SettingsAction.OnDismissUnsyncedDialog) },
        )
    }

    if (state.showSyncErrorDialog) {
        SyncErrorDialog(
            onCancel = { onAction(SettingsAction.OnDismissSyncErrorDialog) },
            onLogoutWithoutSyncing = { onAction(SettingsAction.OnLogoutWithoutSyncingClick) },
        )
    }
}

@Composable
private fun SyncIntervalDropdown(
    selectedInterval: SyncInterval,
    onIntervalSelected: (SyncInterval) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = SurfaceLowest,
                shape = RoundedCornerShape(16.dp),
            )
            .width(200.dp),
    ) {
        SyncInterval.entries.forEachIndexed { index, interval ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntervalSelected(interval) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = interval.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                    color = OnSurface,
                    modifier = Modifier.weight(1f),
                )
                if (interval == selectedInterval) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = OnSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            if (index < SyncInterval.entries.lastIndex) {
                HorizontalDivider(
                    color = Surface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
