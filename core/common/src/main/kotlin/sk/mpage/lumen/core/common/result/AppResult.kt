package sk.mpage.lumen.core.common.result

/**
 * Outcome of a one-shot data operation. Unlike [kotlin.Result], the failure side is a
 * [DataError], so callers can handle every case with an exhaustive `when`.
 */
public sealed interface AppResult<out T> {
    public data class Success<T>(
        val value: T,
    ) : AppResult<T>

    public data class Failure(
        val error: DataError,
    ) : AppResult<Nothing>
}

public inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Failure -> this
    }
