package com.juandgaines.notemark.note.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.juandgaines.notemark.note.presentation.note_detail.NoteDetailMode
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurfaceVariant
import com.juandgaines.notemark.ui.theme.SurfaceLowest

@Composable
fun NoteDetailExtendedFab(
    currentMode: NoteDetailMode,
    onEditClick: () -> Unit,
    onReaderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLowest)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (currentMode == NoteDetailMode.EDIT) OnSurface else OnSurfaceVariant.copy(alpha = 0.5f),
            )
        }
        IconButton(onClick = onReaderClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (currentMode == NoteDetailMode.READER) OnSurface else OnSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}
