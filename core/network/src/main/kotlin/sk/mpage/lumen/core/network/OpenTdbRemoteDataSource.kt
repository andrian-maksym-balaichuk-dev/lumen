package sk.mpage.lumen.core.network

import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.network.dto.CategoryDto
import sk.mpage.lumen.core.network.dto.QuestionDto
import sk.mpage.lumen.core.network.dto.TokenResponseDto
import sk.mpage.lumen.core.network.error.OpenTdbResponseCode
import sk.mpage.lumen.core.network.error.safeCall
import sk.mpage.lumen.core.network.model.RemoteCategory
import sk.mpage.lumen.core.network.model.RemoteQuestion
import sk.mpage.lumen.core.network.model.RemoteQuestions
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
        ): AppResult<RemoteQuestions> =
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
                    when (val code = result.value.responseCode) {
                        OpenTdbResponseCode.TOKEN_NOT_FOUND -> {
                            AppResult.Success(RemoteQuestions.TokenNotFound)
                        }

                        OpenTdbResponseCode.TOKEN_EMPTY -> {
                            AppResult.Success(RemoteQuestions.TokenExhausted)
                        }

                        else -> {
                            OpenTdbResponseCode.toDataError(code)?.let { AppResult.Failure(it) }
                                ?: AppResult.Success(
                                    RemoteQuestions.Loaded(result.value.results.map(QuestionDto::toRemote)),
                                )
                        }
                    }
                }

                is AppResult.Failure -> {
                    result
                }
            }

        public suspend fun requestToken(): AppResult<String> = token { api.token(command = "request") }

        public suspend fun resetToken(token: String): AppResult<String> =
            token { api.token(command = "reset", token = token) }

        private suspend fun token(call: suspend () -> TokenResponseDto): AppResult<String> =
            when (val result = rateLimiter.throttle { safeCall { call() } }) {
                is AppResult.Success -> {
                    val token = result.value.token
                    val error = OpenTdbResponseCode.toDataError(result.value.responseCode)
                    when {
                        error != null -> AppResult.Failure(error)
                        token.isNullOrEmpty() -> AppResult.Failure(DataError.Serialization)
                        else -> AppResult.Success(token)
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
