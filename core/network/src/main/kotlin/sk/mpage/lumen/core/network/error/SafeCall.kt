package sk.mpage.lumen.core.network.error

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

internal suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        AppResult.Failure(e.toDataError())
    } catch (e: HttpException) {
        AppResult.Failure(e.toDataError())
    } catch (_: SerializationException) {
        AppResult.Failure(DataError.Serialization)
    }
