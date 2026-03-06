package com.juandgaines.notemark.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LogoutRequest(
    val refreshToken: String,
)
