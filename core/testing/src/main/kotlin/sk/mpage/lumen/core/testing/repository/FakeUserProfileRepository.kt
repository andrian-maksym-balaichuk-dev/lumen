package sk.mpage.lumen.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.AvatarInput
import sk.mpage.lumen.core.model.UserProfile

public class FakeUserProfileRepository(
    initial: UserProfile = UserProfile(name = "", avatar = Avatar.None, onboarded = false),
) : UserProfileRepository {
    private val profile = MutableStateFlow(initial)
    private var nextFailure: DataError? = null

    public val current: UserProfile get() = profile.value

    public fun emit(value: UserProfile) {
        profile.value = value
    }

    public fun failNext(error: DataError) {
        nextFailure = error
    }

    override fun observeProfile(): Flow<UserProfile> = profile

    override suspend fun completeOnboarding(
        name: String,
        avatar: Avatar,
    ): AppResult<Unit> =
        result(Unit) {
            profile.value = UserProfile(name = name, avatar = avatar, onboarded = true)
        }

    override suspend fun updateName(name: String): AppResult<Unit> =
        result(Unit) {
            profile.update { it.copy(name = name) }
        }

    override suspend fun updateAvatar(avatar: AvatarInput): AppResult<Avatar> {
        val next =
            when (avatar) {
                is AvatarInput.Preset -> Avatar.Preset(avatar.key)
                is AvatarInput.Picked -> Avatar.Custom(avatar.uriString.substringAfterLast('/'))
            }
        return result(next) { profile.update { it.copy(avatar = next) } }
    }

    private inline fun <T> result(
        value: T,
        apply: () -> Unit,
    ): AppResult<T> {
        nextFailure?.let {
            nextFailure = null
            return AppResult.Failure(it)
        }
        apply()
        return AppResult.Success(value)
    }
}
