package com.juandgaines.notemark.core.data.networking

import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend inline fun <reified Response: Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Remote> {
    return safeCall {
        get {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified Request, reified Response: Any> HttpClient.post(
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

suspend inline fun <reified Request, reified Response: Any> HttpClient.put(
    route: String,
    body: Request
): Result<Response, DataError.Remote> {
    return safeCall {
        put {
            url(constructRoute(route))
            setBody(body)
        }
    }
}

suspend inline fun <reified Response: Any> HttpClient.delete(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Remote> {
    return safeCall {
        delete {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Remote> {
    val response = try {
        execute()
    } catch(e: UnknownHostException) {
        return Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: UnresolvedAddressException) {
        return Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: ConnectException) {
        return Result.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: SocketTimeoutException) {
        return Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch(e: SerializationException) {
        return Result.Failure(DataError.Remote.SERIALIZATION)
    } catch(e: Exception) {
        if(e is CancellationException) throw e
        return Result.Failure(DataError.Remote.UNKNOWN)
    }

    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Remote> {
    return when(response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch(e: NoTransformationFoundException) {
                Result.Failure(DataError.Remote.SERIALIZATION)
            }
        }
        400 -> Result.Failure(DataError.Remote.BAD_REQUEST)
        401 -> Result.Failure(DataError.Remote.UNAUTHORIZED)
        403 -> Result.Failure(DataError.Remote.FORBIDDEN)
        404 -> Result.Failure(DataError.Remote.NOT_FOUND)
        408 -> Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
        409 -> Result.Failure(DataError.Remote.CONFLICT)
        413 -> Result.Failure(DataError.Remote.PAYLOAD_TOO_LARGE)
        429 -> Result.Failure(DataError.Remote.TOO_MANY_REQUESTS)
        500 -> Result.Failure(DataError.Remote.SERVER_ERROR)
        503 -> Result.Failure(DataError.Remote.SERVICE_UNAVAILABLE)
        else -> Result.Failure(DataError.Remote.UNKNOWN)
    }
}

fun constructRoute(route: String): String {
    val baseUrl = com.juandgaines.notemark.BuildConfig.BASE_URL
    return when {
        route.contains(baseUrl) -> route
        route.startsWith("/") -> baseUrl + route
        else -> baseUrl + "/$route"
    }
}
