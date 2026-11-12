package sk.mpage.lumen.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CategoriesResponseDto(
    @SerialName("trivia_categories") val categories: List<CategoryDto>,
)

@Serializable
internal data class CategoryDto(
    val id: Int,
    val name: String,
)
