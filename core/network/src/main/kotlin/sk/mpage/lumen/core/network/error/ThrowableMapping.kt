package sk.mpage.lumen.core.network.error

import retrofit2.HttpException
import sk.mpage.lumen.core.common.result.DataError
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

private const val HTTP_TOO_MANY_REQUESTS = 429

internal fun IOException.toDataError(): DataError =
    when (this) {
        is SocketTimeoutException -> DataError.Timeout
        is InterruptedIOException -> DataError.Timeout
        else -> DataError.NoInternet
    }

internal fun HttpException.toDataError(): DataError =
    if (code() == HTTP_TOO_MANY_REQUESTS) DataError.RateLimited else DataError.Http(code())
