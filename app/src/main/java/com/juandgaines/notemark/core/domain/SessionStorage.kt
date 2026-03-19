package com.juandgaines.notemark.core.domain

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    suspend fun get(): AuthInfo?
    suspend fun set(info: AuthInfo?)
    fun observe(): Flow<AuthInfo?>
}
