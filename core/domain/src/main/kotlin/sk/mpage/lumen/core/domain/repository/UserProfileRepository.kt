package sk.mpage.lumen.core.domain.repository

import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.AvatarInput
import sk.mpage.lumen.core.model.UserProfile

public interface UserProfileRepository {
    public fun observeProfile(): Flow<UserProfile>

    public suspend fun completeOnboarding(
        name: String,
        avatar: Avatar,
    ): AppResult<Unit>

    public suspend fun updateName(name: String): AppResult<Unit>

    public suspend fun updateAvatar(avatar: AvatarInput): AppResult<Avatar>
}
