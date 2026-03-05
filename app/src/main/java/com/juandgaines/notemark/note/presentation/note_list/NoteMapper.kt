package com.juandgaines.notemark.note.presentation.note_list

import com.juandgaines.notemark.note.domain.Note
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val thisYearFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)
private val otherYearFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

fun Note.toNoteUi(): NoteUi {
    val currentYear = LocalDate.now().year
    val formatter = if (createdAt.year == currentYear) thisYearFormatter else otherYearFormatter
    return NoteUi(
        id = id,
        formattedDate = createdAt.format(formatter),
        title = title,
        content = content,
    )
}
