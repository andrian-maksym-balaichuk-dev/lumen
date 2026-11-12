package sk.mpage.lumen.core.network

import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.network.dto.CategoryDto
import sk.mpage.lumen.core.network.dto.QuestionDto
import sk.mpage.lumen.core.network.error.OpenTdbResponseCode
import sk.mpage.lumen.core.network.error.safeCall
import sk.mpage.lumen.core.network.model.RemoteCategory
import sk.mpage.lumen.core.network.model.RemoteQuestion
import javax.inject.Inject

public class OpenTdbRemoteDataSource
    @Inject
    internal constructor(
        private val api: OpenTdbApi,
        private val rateLimiter: OpenTdbRateLimiter,
    ) {
        public suspend fun categories(): AppResult<List<RemoteCategory>> =
            when (val result = rateLimiter.throttle { safeCall { api.categories() } }) {
                is AppResult.Success -> AppResult.Success(result.value.categories.map(CategoryDto::toRemote))
                is AppResult.Failure -> result
            }

        public suspend fun questions(
            amount: Int,
            category: Int?,
            difficulty: String?,
            type: String,
            token: String?,
        ): AppResult<List<RemoteQuestion>> =
            when (
                val result =
                    rateLimiter.throttle {
                        safeCall {
                            api.questions(
                                amount,
                                category,
                                difficulty,
                                type,
                                token,
                            )
                        }
                    }
            ) {
                is AppResult.Success -> {
                    val error = OpenTdbResponseCode.toDataError(result.value.responseCode)
                    if (error != null) {
                        AppResult.Failure(error)
                    } else {
                        AppResult.Success(result.value.results.map(QuestionDto::toRemote))
                    }
                }

                is AppResult.Failure -> {
                    result
                }
            }
    }

private fun CategoryDto.toRemote(): RemoteCategory = RemoteCategory(id = id, name = name)

private fun QuestionDto.toRemote(): RemoteQuestion =
    RemoteQuestion(
        type = type,
        difficulty = difficulty,
        category = category,
        question = question,
        correctAnswer = correctAnswer,
        incorrectAnswers = incorrectAnswers,
    )
