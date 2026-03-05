package com.juandgaines.notemark.note.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.juandgaines.notemark.R
import com.juandgaines.notemark.ui.theme.ErrorRed

@Composable
fun DiscardChangesDialog(
    onConfirmDiscard: () -> Unit,
    onKeepEditing: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        title = { Text(text = stringResource(R.string.discard_changes_title)) },
        text = { Text(text = stringResource(R.string.discard_changes_message)) },
        confirmButton = {
            TextButton(onClick = onConfirmDiscard) {
                Text(
                    text = stringResource(R.string.discard),
                    color = ErrorRed,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepEditing) {
                Text(
                    text = stringResource(R.string.keep_editing),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}
