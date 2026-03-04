package com.juandgaines.notemark.core.presentation.util

import com.juandgaines.notemark.core.domain.util.DataError
import com.juandgaines.notemark.R

fun DataError.toUiText(): UiText {
    val stringRes = when(this) {
        DataError.Remote.BAD_REQUEST -> R.string.error_bad_request
        DataError.Remote.REQUEST_TIMEOUT -> R.string.error_request_timeout
        DataError.Remote.UNAUTHORIZED -> R.string.error_unauthorized
        DataError.Remote.FORBIDDEN -> R.string.error_forbidden
        DataError.Remote.NOT_FOUND -> R.string.error_not_found
        DataError.Remote.CONFLICT -> R.string.error_conflict
        DataError.Remote.TOO_MANY_REQUESTS -> R.string.error_too_many_requests
        DataError.Remote.NO_INTERNET -> R.string.error_no_internet
        DataError.Remote.PAYLOAD_TOO_LARGE -> R.string.error_payload_too_large
        DataError.Remote.SERVER_ERROR -> R.string.error_server_error
        DataError.Remote.SERVICE_UNAVAILABLE -> R.string.error_service_unavailable
        DataError.Remote.SERIALIZATION -> R.string.error_serialization
        DataError.Remote.UNKNOWN -> R.string.error_unknown
        DataError.Local.DISK_FULL -> R.string.error_disk_full
        DataError.Local.NOT_FOUND -> R.string.error_not_found
        DataError.Local.UNKNOWN -> R.string.error_unknown
    }
    return UiText.StringResource(stringRes)
}
