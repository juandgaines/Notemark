package com.juandgaines.notemark.auth.data

import com.juandgaines.notemark.auth.data.dto.AuthResponse
import com.juandgaines.notemark.auth.data.dto.LoginRequest
import com.juandgaines.notemark.auth.data.dto.RegisterRequest
import com.juandgaines.notemark.auth.domain.AuthRepository
import com.juandgaines.notemark.core.data.networking.post
import com.juandgaines.notemark.core.domain.AuthInfo
import com.juandgaines.notemark.core.domain.SessionStorage
import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.EmptyResult
import com.juandgaines.notemark.core.domain.util.Result
import com.juandgaines.notemark.core.domain.util.asEmptyResult
import com.juandgaines.notemark.core.domain.util.map
import io.ktor.client.HttpClient

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
) : AuthRepository {

    override suspend fun register(
        username: String,
        email: String,
        password: String,
    ): EmptyResult<DataError.Remote> {
        return httpClient.post<RegisterRequest, Unit>(
            route = "/api/auth/register",
            body = RegisterRequest(
                username = username,
                email = email,
                password = password,
            )
        ).asEmptyResult()
    }

    override suspend fun login(
        email: String,
        password: String,
    ): EmptyResult<DataError.Remote> {
        val result = httpClient.post<LoginRequest, AuthResponse>(
            route = "/api/auth/login",
            body = LoginRequest(
                email = email,
                password = password,
            )
        )

        if (result is Result.Success) {
            sessionStorage.set(
                AuthInfo(
                    accessToken = result.data.accessToken,
                    refreshToken = result.data.refreshToken,
                )
            )
        }

        return result.map { }.asEmptyResult()
    }
}
