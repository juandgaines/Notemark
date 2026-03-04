package com.juandgaines.notemark.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Landing : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object Register : Route

    @Serializable
    data object Main : Route
}
