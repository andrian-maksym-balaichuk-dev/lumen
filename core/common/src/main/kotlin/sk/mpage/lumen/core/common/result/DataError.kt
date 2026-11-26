package sk.mpage.lumen.core.common.result

import kotlinx.serialization.Serializable

@Serializable
public sealed interface DataError {
    @Serializable
    public data object NoInternet : DataError

    @Serializable
    public data object Timeout : DataError

    /** Open Trivia DB response code 5 or HTTP 429. */
    @Serializable
    public data object RateLimited : DataError

    /** Open Trivia DB response code 1: not enough questions for the query. */
    @Serializable
    public data object NoResults : DataError

    /** Open Trivia DB response codes 3 and 4, after renewing or resetting the token failed. */
    @Serializable
    public data object TokenInvalid : DataError

    /** Open Trivia DB response code 2. */
    @Serializable
    public data object InvalidRequest : DataError

    @Serializable
    public data class Http(
        val code: Int,
    ) : DataError

    @Serializable
    public data object Serialization : DataError

    /** Disk or IO failure in Room, DataStore or avatar storage. */
    @Serializable
    public data object Storage : DataError

    /** A failure at the network boundary that matches no other case. */
    @Serializable
    public data object Unknown : DataError
}
