# JWT Token Refresh (Android)

Pattern for implementing JWT bearer token refresh with Ktor Auth plugin and session expiration detection.

## HttpClientFactory with Token Refresh

```kotlin
package com.juandgaines.notemark.core.data.networking

import com.juandgaines.notemark.BuildConfig
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
                header("X-User-Email", BuildConfig.USER_EMAIL)
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
                        if (path.contains("/auth/login") || path.contains("/auth/register")) {
                            return@refreshTokens BearerTokens("", "")
                        }

                        val info = sessionStorage.get()
                        val refreshResult = client.post<RefreshTokenRequest, AuthResponse>(
                            route = "/api/auth/refresh",
                            body = RefreshTokenRequest(
                                refreshToken = info?.refreshToken ?: "",
                            )
                        ) {
                            markAsRefreshTokenRequest()
                        }

                        if (refreshResult is Result.Success) {
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

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)
```

## AuthResponse DTO

```kotlin
package com.juandgaines.notemark.auth.data.dto

import kotlinx.serialization.Serializable

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

## X-User-Email Header (Build Config)

In `local.properties`:
```properties
USER_EMAIL=your.email@example.com
```

In `app/build.gradle.kts`:
```kotlin
android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val localProperties = java.util.Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) load(file.inputStream())
        }
        buildConfigField("String", "USER_EMAIL", "\"${localProperties["USER_EMAIL"] ?: ""}\"")
        buildConfigField("String", "BASE_URL", "\"https://your-api.example.com\"")
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

## Notes

- `markAsRefreshTokenRequest()` tells Ktor Auth not to retry the refresh request itself
- Skip auth endpoints (`/auth/login`, `/auth/register`) in `refreshTokens` to avoid infinite loops
- On refresh failure, clear the session — the `observe()` flow in MainViewModel detects the null transition
- `X-User-Email` header is read from BuildConfig, sourced from `local.properties`
- The refresh endpoint path (`/api/auth/refresh`) should match your API
