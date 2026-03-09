package com.juandgaines.notemark.settings.presentation.settings_screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.juandgaines.notemark.R
import com.juandgaines.notemark.ui.theme.BrandPrimary
import com.juandgaines.notemark.ui.theme.ErrorRed

@Composable
fun UnsyncedChangesDialog(
    onSyncNow: () -> Unit,
    onLogoutWithoutSyncing: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.unsynced_changes_title))
        },
        text = {
            Text(text = stringResource(R.string.unsynced_changes_message))
        },
        confirmButton = {
            TextButton(onClick = onSyncNow) {
                Text(
                    text = stringResource(R.string.sync_now),
                    color = BrandPrimary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onLogoutWithoutSyncing) {
                Text(
                    text = stringResource(R.string.logout_without_syncing),
                    color = ErrorRed,
                )
            }
        },
    )
}

@Composable
fun SyncErrorDialog(
    onCancel: () -> Unit,
    onLogoutWithoutSyncing: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(R.string.sync_error_title))
        },
        text = {
            Text(text = stringResource(R.string.sync_error_message))
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = BrandPrimary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onLogoutWithoutSyncing) {
                Text(
                    text = stringResource(R.string.logout_without_syncing),
                    color = ErrorRed,
                )
            }
        },
    )
}
