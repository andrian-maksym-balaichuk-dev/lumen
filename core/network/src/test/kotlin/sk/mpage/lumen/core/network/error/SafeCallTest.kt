package sk.mpage.lumen.core.network.error

import kotlinx.coroutines.test.runTest
import sk.mpage.lumen.core.common.result.AppResult
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SafeCallTest {
    @Test
    fun `wraps a value in success`() =
        runTest {
            assertEquals(AppResult.Success(7), safeCall { 7 })
        }

    @Test
    fun `rethrows cancellation instead of mapping it`() =
        runTest {
            assertFailsWith<CancellationException> {
                safeCall<Unit> { throw CancellationException("cancelled") }
            }
        }

    @Test
    fun `does not swallow unexpected exceptions`() =
        runTest {
            assertFailsWith<IllegalStateException> {
                safeCall<Unit> { error("bug") }
            }
        }
}
