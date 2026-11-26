package sk.mpage.lumen.core.data.token

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sk.mpage.lumen.core.common.dispatcher.ApplicationScope
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.datastore.PreferenceKeys
import sk.mpage.lumen.core.datastore.UserPreferencesDataSource
import sk.mpage.lumen.core.network.OpenTdbRemoteDataSource
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

@Singleton
internal class OpenTdbSessionTokenManager
    @Inject
    constructor(
        private val remote: OpenTdbRemoteDataSource,
        private val preferences: UserPreferencesDataSource,
        private val clock: Clock,
        @ApplicationScope private val appScope: CoroutineScope,
    ) {
        private val mutex = Mutex()
        private var current: String? = null

        suspend fun current(): String? = mutex.withLock { current ?: stored() }

        fun requestInBackground() {
            appScope.launch {
                mutex.withLock { if (current == null && stored() == null) request() }
            }
        }

        suspend fun onTokenNotFound() {
            mutex.withLock {
                current = null
                preferences.clearSessionToken()
            }
        }

        suspend fun reset(): String? =
            mutex.withLock {
                val token = current ?: stored() ?: return@withLock request()
                when (val result = remote.resetToken(token)) {
                    is AppResult.Success -> save(result.value)
                    is AppResult.Failure -> null
                }
            }

        private suspend fun stored(): String? {
            val prefs = preferences.preferences.first()
            val token = prefs[PreferenceKeys.SESSION_TOKEN] ?: return null
            val issuedAt = prefs[PreferenceKeys.SESSION_TOKEN_AT] ?: return null
            if (clock.millis() - issuedAt >= MAX_AGE_MS) return null
            current = token
            return token
        }

        private suspend fun request(): String? =
            when (val result = remote.requestToken()) {
                is AppResult.Success -> save(result.value)
                is AppResult.Failure -> null
            }

        private suspend fun save(token: String): String {
            current = token
            preferences.setSessionToken(token, clock.millis())
            return token
        }

        private companion object {
            val MAX_AGE_MS = 6.hours.inWholeMilliseconds
        }
    }
