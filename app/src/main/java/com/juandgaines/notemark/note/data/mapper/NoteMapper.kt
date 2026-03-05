package com.juandgaines.notemark.note.data.mapper

import com.juandgaines.notemark.note.data.dto.NoteDto
import com.juandgaines.notemark.note.data.local.NoteEntity
import com.juandgaines.notemark.note.domain.Note
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

private fun parseUtcTimestamp(value: String): LocalDateTime {
    return if (value.endsWith("Z")) {
        ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime()
    } else {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

fun NoteDto.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = parseUtcTimestamp(createdAt),
        lastEditedAt = parseUtcTimestamp(lastEditedAt),
    )
}

fun NoteDto.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        lastEditedAt = lastEditedAt,
    )
}

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = parseUtcTimestamp(createdAt),
        lastEditedAt = parseUtcTimestamp(lastEditedAt),
    )
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt.atOffset(ZoneOffset.UTC).format(utcFormatter),
        lastEditedAt = lastEditedAt.atOffset(ZoneOffset.UTC).format(utcFormatter),
    )
}
