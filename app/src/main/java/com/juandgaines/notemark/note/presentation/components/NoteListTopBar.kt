package com.juandgaines.notemark.note.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.juandgaines.notemark.R
import com.juandgaines.notemark.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListTopBar(
    profileInitials: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "NoteMark",
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                )
            }
            ProfileAvatar(initials = profileInitials)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Surface,
        ),
    )
}
