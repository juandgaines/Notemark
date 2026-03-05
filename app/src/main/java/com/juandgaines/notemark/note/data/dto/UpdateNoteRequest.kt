package com.juandgaines.notemark.note.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteRequest(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val lastEditedAt: String,
)
