package com.juandgaines.notemark.core.data.networking

import com.juandgaines.notemark.auth.data.dto.RefreshTokenRequest
import com.juandgaines.notemark.auth.data.dto.RefreshTokenResponse
import com.juandgaines.notemark.core.domain.AuthInfo
import com.juandgaines.notemark.core.domain.SessionStorage
import com.juandgaines.notemark.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber

class HttpClientFactory(
    private val sessionStorage: SessionStorage,
    private val json: Json,
) {

    fun build(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json = json)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
                level = LogLevel.ALL
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                header("x-api-key", "notemark-2025")
                header("X-User-Email", com.juandgaines.notemark.BuildConfig.USER_EMAIL)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val info = sessionStorage.get()
                        BearerTokens(
                            accessToken = info?.accessToken ?: "",
                            refreshToken = info?.refreshToken ?: "",
                        )
                    }
                    refreshTokens {
                        val path = response.call.request.url.encodedPath
                        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
                            return@refreshTokens BearerTokens("", "")
                        }

                        val info = sessionStorage.get()
                        val refreshResult = client.post<RefreshTokenRequest, RefreshTokenResponse>(
                            route = "/api/auth/refresh",
                            body = RefreshTokenRequest(
                                refreshToken = info?.refreshToken ?: "",
                            )
                        ) {
                            markAsRefreshTokenRequest()
                        }

                        if (refreshResult is Result.Success) {
                            val existingInfo = sessionStorage.get()
                            val newAuthInfo = AuthInfo(
                                accessToken = refreshResult.data.accessToken,
                                refreshToken = refreshResult.data.refreshToken,
                                username = existingInfo?.username ?: "",
                            )
                            sessionStorage.set(newAuthInfo)

                            BearerTokens(
                                accessToken = newAuthInfo.accessToken,
                                refreshToken = newAuthInfo.refreshToken,
                            )
                        } else {
                            sessionStorage.set(null)
                            BearerTokens("", "")
                        }
                    }
                }
            }
        }
    }
}
