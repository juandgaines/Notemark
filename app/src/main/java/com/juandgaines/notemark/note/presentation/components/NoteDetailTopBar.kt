package com.juandgaines.notemark.note.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.juandgaines.notemark.R
import com.juandgaines.notemark.ui.theme.BrandPrimary
import com.juandgaines.notemark.ui.theme.SurfaceLowest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailTopBar(
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit,
    canSave: Boolean,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {},
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                )
            }
        },
        actions = {
            TextButton(
                onClick = onSaveClick,
                enabled = canSave,
            ) {
                Text(
                    text = stringResource(R.string.save_note),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canSave) BrandPrimary else BrandPrimary.copy(alpha = 0.4f),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceLowest,
        ),
    )
}
