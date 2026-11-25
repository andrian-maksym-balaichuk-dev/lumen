package sk.mpage.lumen.core.data.repository

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.data.avatar.AvatarStorage
import sk.mpage.lumen.core.datastore.PreferenceKeys
import sk.mpage.lumen.core.datastore.UserPreferencesDataSource
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.AvatarInput
import sk.mpage.lumen.core.model.UserProfile
import java.io.IOException
import javax.inject.Inject

internal class DataStoreUserProfileRepository
    @Inject
    constructor(
        private val preferences: UserPreferencesDataSource,
        private val avatarStorage: AvatarStorage,
    ) : UserProfileRepository {
        override fun observeProfile(): Flow<UserProfile> =
            preferences.preferences.map { prefs ->
                UserProfile(
                    name = prefs[PreferenceKeys.PROFILE_NAME].orEmpty(),
                    avatar = decodeAvatar(prefs[PreferenceKeys.PROFILE_AVATAR].orEmpty()),
                    onboarded = prefs[PreferenceKeys.ONBOARDED] ?: false,
                )
            }

        override suspend fun completeOnboarding(
            name: String,
            avatar: Avatar,
        ): AppResult<Unit> = storage { preferences.setProfile(name, encodeAvatar(avatar)) }

        override suspend fun updateName(name: String): AppResult<Unit> = storage { preferences.setProfileName(name) }

        override suspend fun updateAvatar(avatar: AvatarInput): AppResult<Avatar> {
            val previous = observeProfile().first().avatar
            val next =
                when (avatar) {
                    is AvatarInput.Preset -> {
                        Avatar.Preset(avatar.key)
                    }

                    is AvatarInput.Picked -> {
                        try {
                            Avatar.Custom(avatarStorage.import(avatar.uriString))
                        } catch (_: IOException) {
                            return AppResult.Failure(DataError.Storage)
                        }
                    }
                }
            try {
                preferences.setProfileAvatar(encodeAvatar(next))
            } catch (e: Throwable) {
                if (next is Avatar.Custom) withContext(NonCancellable) { avatarStorage.delete(next.fileName) }
                if (e is IOException) return AppResult.Failure(DataError.Storage)
                throw e
            }
            if (previous is Avatar.Custom && previous != next) avatarStorage.delete(previous.fileName)
            return AppResult.Success(next)
        }

        private inline fun <T> storage(block: () -> T): AppResult<T> =
            try {
                AppResult.Success(block())
            } catch (_: IOException) {
                AppResult.Failure(DataError.Storage)
            }
    }

private const val PRESET_PREFIX = "preset:"
private const val FILE_PREFIX = "file:"

internal fun encodeAvatar(avatar: Avatar): String =
    when (avatar) {
        is Avatar.Preset -> PRESET_PREFIX + avatar.key
        is Avatar.Custom -> FILE_PREFIX + avatar.fileName
        Avatar.None -> ""
    }

internal fun decodeAvatar(value: String): Avatar =
    when {
        value.startsWith(
            PRESET_PREFIX,
        ) && value.length > PRESET_PREFIX.length -> Avatar.Preset(value.removePrefix(PRESET_PREFIX))

        value.startsWith(
            FILE_PREFIX,
        ) && value.length > FILE_PREFIX.length -> Avatar.Custom(value.removePrefix(FILE_PREFIX))

        else -> Avatar.None
    }
