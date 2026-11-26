package sk.mpage.lumen.core.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sk.mpage.lumen.core.common.result.DataError
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class DataErrorTextTest {
    @Test
    fun `every data error has a message`() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        val errors =
            listOf(
                DataError.NoInternet,
                DataError.Timeout,
                DataError.RateLimited,
                DataError.NoResults,
                DataError.TokenInvalid,
                DataError.InvalidRequest,
                DataError.Http(503),
                DataError.Serialization,
                DataError.Storage,
                DataError.Unknown,
            )

        errors.forEach { error -> assertTrue(error.toUiText().asString(resources).isNotBlank(), "$error") }
    }
}
