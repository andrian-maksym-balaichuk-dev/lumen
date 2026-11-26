package sk.mpage.lumen.core.ui

import sk.mpage.lumen.core.common.result.DataError

public fun DataError.toUiText(): UiText =
    UiText.Resource(
        when (this) {
            DataError.NoInternet -> R.string.error_no_internet
            DataError.Timeout -> R.string.error_timeout
            DataError.RateLimited -> R.string.error_rate_limited
            DataError.NoResults -> R.string.error_no_results
            DataError.TokenInvalid -> R.string.error_token_invalid
            DataError.InvalidRequest -> R.string.error_invalid_request
            is DataError.Http -> R.string.error_http
            DataError.Serialization -> R.string.error_serialization
            DataError.Storage -> R.string.error_storage
            DataError.Unknown -> R.string.error_unknown
        },
    )
