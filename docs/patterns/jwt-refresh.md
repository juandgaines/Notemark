# JWT Token Refresh (Android)

> **Note:** This pattern shows the Ktor Auth bearer token refresh architecture. DTO fields, endpoint paths, and headers must be adapted to your API's contract.

Pattern for implementing JWT bearer token refresh with Ktor Auth plugin and session expiration detection.

## HttpClientFactory with Token Refresh

```kotlin
package com.juandgaines.notemark.core.data.networking

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
) {

    fun build(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
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
                // TODO: Add custom headers required by your API (e.g., API keys, client IDs)
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
                        // Don't refresh for auth endpoints
                        val path = response.call.request.url.encodedPath
                        // TODO: Replace with your actual auth endpoint paths
                        if (path.contains("/your-auth-login-path") || path.contains("/your-auth-register-path")) {
                            return@refreshTokens BearerTokens("", "")
                        }

                        val info = sessionStorage.get()
                        val refreshResult = client.post<RefreshTokenRequest, AuthResponse>(
                            // TODO: Replace with your API's token refresh endpoint
                            route = "/your-token-refresh-path",
                            body = RefreshTokenRequest(
                                refreshToken = info?.refreshToken ?: "",
                            )
                        ) {
                            markAsRefreshTokenRequest()
                        }

                        if (refreshResult is Result.Success) {
                            // TODO: Map response fields to your AuthInfo structure
                            val newAuthInfo = AuthInfo(
                                accessToken = refreshResult.data.accessToken,
                                refreshToken = refreshResult.data.refreshToken,
                            )
                            sessionStorage.set(newAuthInfo)

                            BearerTokens(
                                accessToken = newAuthInfo.accessToken,
                                refreshToken = newAuthInfo.refreshToken,
                            )
                        } else {
                            // Refresh failed — clear session (triggers session expiration)
                            sessionStorage.set(null)
                            BearerTokens("", "")
                        }
                    }
                }
            }
        }
    }
}
```

## RefreshTokenRequest DTO

```kotlin
package com.juandgaines.notemark.auth.data.dto

import kotlinx.serialization.Serializable

// TODO: Match fields to your API's refresh endpoint request body
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)
```

## AuthResponse DTO

```kotlin
package com.juandgaines.notemark.auth.data.dto

import kotlinx.serialization.Serializable

// TODO: Match fields to your API's auth response (e.g., accessToken, refreshToken, userId, expiresIn)
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
)
```

## Session Expiration Detection (MainViewModel)

```kotlin
package com.juandgaines.notemark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.notemark.core.domain.SessionStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface MainEvent {
    data object OnSessionExpired : MainEvent
}

data class MainState(
    val isCheckingAuth: Boolean = true,
    val isLoggedIn: Boolean = false,
)

class MainViewModel(
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    private val eventChannel = Channel<MainEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val authInfo = sessionStorage.get()
            state = state.copy(
                isLoggedIn = authInfo != null,
                isCheckingAuth = false,
            )
        }

        // Observe session changes — detect expiration (non-null → null)
        sessionStorage.observe()
            .onEach { authInfo ->
                if (authInfo == null && state.isLoggedIn) {
                    // Session was cleared (token refresh failed)
                    state = state.copy(isLoggedIn = false)
                    eventChannel.send(MainEvent.OnSessionExpired)
                }
            }
            .launchIn(viewModelScope)
    }
}
```

## HttpClientExt — Refresh Token Support

The `post` extension needs a variant that accepts a request builder for `markAsRefreshTokenRequest()`:

```kotlin
suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
    route: String,
    body: Request,
    builder: HttpRequestBuilder.() -> Unit = {},
): Result<Response, DataError.Remote> {
    return safeCall {
        post {
            url(constructRoute(route))
            setBody(body)
            builder()
        }
    }
}
```

## Logout — Clearing Bearer Tokens

Ktor's Auth plugin **persists bearer tokens in memory** even after you clear the session storage. If you only call `sessionStorage.set(null)` on logout, the next request may still attach the old token. You must also clear the in-memory token cache:

```kotlin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

fun HttpClient.clearBearerTokens() {
    authProviders
        .filterIsInstance<BearerAuthProvider>()
        .firstOrNull()
        ?.clearToken()
}
```

Call this in your logout flow **before or alongside** clearing the session storage:

```kotlin
// In your AuthRepository or logout use case:
suspend fun logout() {
    httpClient.clearBearerTokens()
    sessionStorage.set(null)
}
```

Without this, a logged-out user's stale token can leak into requests made before the next app restart.

## Notes

- `markAsRefreshTokenRequest()` tells Ktor Auth not to retry the refresh request itself
- Skip auth endpoints in `refreshTokens` to avoid infinite loops — replace the placeholder paths with your actual auth routes
- On refresh failure, clear the session — the `observe()` flow in MainViewModel detects the null transition
- The refresh endpoint path and DTO fields must match your API contract
- **Ktor persists bearer tokens in memory** — always call `clearToken()` on logout (see section above)
