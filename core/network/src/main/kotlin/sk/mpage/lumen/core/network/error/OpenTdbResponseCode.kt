package sk.mpage.lumen.core.network.error

import sk.mpage.lumen.core.common.result.DataError

internal object OpenTdbResponseCode {
    const val SUCCESS: Int = 0
    const val NO_RESULTS: Int = 1
    const val INVALID_PARAMETER: Int = 2
    const val TOKEN_NOT_FOUND: Int = 3
    const val TOKEN_EMPTY: Int = 4
    const val RATE_LIMIT: Int = 5

    fun toDataError(code: Int): DataError? =
        when (code) {
            SUCCESS -> null
            NO_RESULTS -> DataError.NoResults
            INVALID_PARAMETER -> DataError.InvalidRequest
            TOKEN_NOT_FOUND, TOKEN_EMPTY -> DataError.TokenInvalid
            RATE_LIMIT -> DataError.RateLimited
            else -> DataError.Unknown
        }
}
