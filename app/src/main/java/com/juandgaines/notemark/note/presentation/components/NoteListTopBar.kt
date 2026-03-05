package com.juandgaines.notemark.note.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.juandgaines.notemark.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListTopBar(
    profileInitials: String,
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
            ProfileAvatar(initials = profileInitials)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Surface,
        ),
    )
}
