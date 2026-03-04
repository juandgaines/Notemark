package com.juandgaines.notemark.auth.domain

import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.EmptyResult

interface AuthRepository {
    suspend fun register(username: String, email: String, password: String): EmptyResult<DataError.Remote>
    suspend fun login(email: String, password: String): EmptyResult<DataError.Remote>
}
