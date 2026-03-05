package com.juandgaines.notemark.note.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteListResponse(
    val notes: List<NoteDto>,
    val total: Int,
)
