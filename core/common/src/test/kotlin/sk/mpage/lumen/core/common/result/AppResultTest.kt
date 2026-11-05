package sk.mpage.lumen.core.common.result

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppResultTest {
    @Test
    fun `map transforms a success`() {
        val result: AppResult<Int> = AppResult.Success(21)

        assertEquals(AppResult.Success(42), result.map { it * 2 })
    }

    @Test
    fun `map keeps a failure and does not call transform`() {
        val result: AppResult<Int> = AppResult.Failure(DataError.Timeout)

        val mapped = result.map<Int, Int> { fail("transform must not run for a failure") }

        assertEquals(AppResult.Failure(DataError.Timeout), mapped)
    }
}
