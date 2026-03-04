package com.juandgaines.notemark.core.domain

// TODO: Add or remove fields to match your auth token structure
data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
)
