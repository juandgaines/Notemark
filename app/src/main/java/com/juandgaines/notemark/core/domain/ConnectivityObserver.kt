package com.juandgaines.notemark.core.domain

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun isConnected(): Flow<Boolean>
}
