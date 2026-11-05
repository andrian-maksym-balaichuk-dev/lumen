package sk.mpage.lumen.core.common.result

public sealed interface DataError {
    public data object NoInternet : DataError

    public data object Timeout : DataError

    /** Open Trivia DB response code 5 or HTTP 429. */
    public data object RateLimited : DataError

    /** Open Trivia DB response code 1: not enough questions for the query. */
    public data object NoResults : DataError

    /** Open Trivia DB response codes 3 and 4, after renewing or resetting the token failed. */
    public data object TokenInvalid : DataError

    /** Open Trivia DB response code 2. */
    public data object InvalidRequest : DataError

    public data class Http(
        val code: Int,
    ) : DataError

    public data object Serialization : DataError

    /** Disk or IO failure in Room, DataStore or avatar storage. */
    public data object Storage : DataError

    /** A failure at the network boundary that matches no other case. */
    public data object Unknown : DataError
}
