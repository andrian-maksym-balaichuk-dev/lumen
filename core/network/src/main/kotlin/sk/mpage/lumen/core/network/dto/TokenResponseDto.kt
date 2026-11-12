package sk.mpage.lumen.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TokenResponseDto(
    @SerialName("response_code") val responseCode: Int,
    val token: String? = null,
)
