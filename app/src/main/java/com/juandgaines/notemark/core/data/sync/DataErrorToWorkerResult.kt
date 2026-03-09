package com.juandgaines.notemark.core.data.sync

import androidx.work.ListenableWorker
import com.juandgaines.notemark.core.domain.util.DataError

fun DataError.toWorkerResult(): ListenableWorker.Result {
    return when (this) {
        DataError.Remote.REQUEST_TIMEOUT,
        DataError.Remote.UNAUTHORIZED,
        DataError.Remote.CONFLICT,
        DataError.Remote.TOO_MANY_REQUESTS,
        DataError.Remote.NO_INTERNET,
        DataError.Remote.SERVER_ERROR,
        DataError.Remote.SERVICE_UNAVAILABLE -> ListenableWorker.Result.retry()

        DataError.Local.DISK_FULL,
        DataError.Remote.PAYLOAD_TOO_LARGE,
        DataError.Remote.SERIALIZATION,
        DataError.Remote.UNKNOWN,
        DataError.Remote.BAD_REQUEST,
        DataError.Remote.FORBIDDEN,
        DataError.Remote.NOT_FOUND,
        DataError.Local.NOT_FOUND,
        DataError.Local.UNKNOWN -> ListenableWorker.Result.failure()
    }
}
