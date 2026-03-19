package com.juandgaines.notemark

sealed interface MainEvent {
    data object OnSessionExpired : MainEvent
}
