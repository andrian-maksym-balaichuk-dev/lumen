package sk.mpage.lumen.core.network

import retrofit2.http.GET
import retrofit2.http.Query
import sk.mpage.lumen.core.network.dto.CategoriesResponseDto
import sk.mpage.lumen.core.network.dto.QuestionsResponseDto
import sk.mpage.lumen.core.network.dto.TokenResponseDto

internal interface OpenTdbApi {
    @GET("api_category.php")
    suspend fun categories(): CategoriesResponseDto

    @GET("api_token.php")
    suspend fun token(
        @Query("command") command: String,
        @Query("token") token: String? = null,
    ): TokenResponseDto

    @GET("api.php")
    suspend fun questions(
        @Query("amount") amount: Int,
        @Query("category") category: Int?,
        @Query("difficulty") difficulty: String?,
        @Query("type") type: String?,
        @Query("token") token: String?,
    ): QuestionsResponseDto
}
