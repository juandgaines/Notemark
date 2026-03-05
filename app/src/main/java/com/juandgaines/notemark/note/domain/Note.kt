package com.juandgaines.notemark.note.domain

import java.time.LocalDateTime

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val lastEditedAt: LocalDateTime,
)
