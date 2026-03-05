package com.juandgaines.notemark.note.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.juandgaines.notemark.note.presentation.note_list.NoteUi
import com.juandgaines.notemark.ui.theme.BrandPrimary
import com.juandgaines.notemark.ui.theme.SurfaceLowest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    noteUi: NoteUi,
    contentPreviewMaxChars: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceLowest,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = noteUi.formattedDate,
                style = MaterialTheme.typography.labelMedium,
                color = BrandPrimary,
            )
            if (noteUi.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = noteUi.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (noteUi.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = noteUi.content.take(contentPreviewMaxChars),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
