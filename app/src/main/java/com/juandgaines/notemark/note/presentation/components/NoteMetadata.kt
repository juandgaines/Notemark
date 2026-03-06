package com.juandgaines.notemark.note.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.juandgaines.notemark.R
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurfaceVariant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun NoteMetadata(
    createdAt: LocalDateTime?,
    lastEditedAt: LocalDateTime?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.date_created),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDateTime(createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.last_edited),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatLastEdited(lastEditedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

private fun formatDateTime(dateTime: LocalDateTime?): String {
    if (dateTime == null) return ""
    return dateTime.format(dateTimeFormatter)
}

private fun formatLastEdited(dateTime: LocalDateTime?): String {
    if (dateTime == null) return ""
    val minutesAgo = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now())
    return if (minutesAgo < 5) "Just now" else dateTime.format(dateTimeFormatter)
}
