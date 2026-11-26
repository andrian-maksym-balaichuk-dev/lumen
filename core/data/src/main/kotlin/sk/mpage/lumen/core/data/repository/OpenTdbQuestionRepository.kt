package sk.mpage.lumen.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import sk.mpage.lumen.core.common.dispatcher.Dispatcher
import sk.mpage.lumen.core.common.dispatcher.LumenDispatcher
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.common.text.HtmlEntityDecoder
import sk.mpage.lumen.core.data.feed.ClassicQuestionFeed
import sk.mpage.lumen.core.data.feed.MarathonQuestionFeed
import sk.mpage.lumen.core.data.mapper.toQuestion
import sk.mpage.lumen.core.data.token.OpenTdbSessionTokenManager
import sk.mpage.lumen.core.domain.game.QuestionFeed
import sk.mpage.lumen.core.domain.repository.QuestionRepository
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.network.OpenTdbRemoteDataSource
import sk.mpage.lumen.core.network.model.RemoteQuestions
import javax.inject.Inject
import kotlin.random.Random

internal class OpenTdbQuestionRepository
    @Inject
    constructor(
        private val remote: OpenTdbRemoteDataSource,
        private val tokens: OpenTdbSessionTokenManager,
        private val decoder: HtmlEntityDecoder,
        private val random: Random,
        @Dispatcher(LumenDispatcher.Default) private val default: CoroutineDispatcher,
    ) : QuestionRepository {
        override fun feed(
            config: QuizConfig,
            scope: CoroutineScope,
        ): QuestionFeed =
            when (config.mode) {
                GameMode.CLASSIC -> ClassicQuestionFeed { amount -> fetch(config, amount) }
                GameMode.MARATHON -> MarathonQuestionFeed(scope) { amount -> fetch(config, amount) }
            }

        private suspend fun fetch(
            config: QuizConfig,
            amount: Int,
        ): AppResult<List<Question>> {
            var token = tokens.current()
            repeat(TOKEN_ATTEMPTS) {
                val result =
                    remote.questions(
                        amount = amount,
                        category = config.categoryId,
                        difficulty = config.difficulty.takeIf { it != Difficulty.ANY }?.code,
                        type = config.type.code,
                        token = token,
                    )
                val response =
                    when (result) {
                        is AppResult.Failure -> return result
                        is AppResult.Success -> result.value
                    }
                when (response) {
                    is RemoteQuestions.Loaded -> {
                        if (token == null) tokens.requestInBackground()
                        return withContext(default) { map(response, config, amount) }
                    }

                    RemoteQuestions.TokenNotFound -> {
                        tokens.onTokenNotFound()
                        token = null
                    }

                    RemoteQuestions.TokenExhausted -> {
                        token = tokens.reset()
                    }
                }
            }
            return AppResult.Failure(DataError.TokenInvalid)
        }

        private fun map(
            response: RemoteQuestions.Loaded,
            config: QuizConfig,
            amount: Int,
        ): AppResult<List<Question>> {
            val questions = response.questions.map { it.toQuestion(decoder, random, config.type) }
            if (questions.size < amount) return AppResult.Failure(DataError.NoResults)
            if (questions.any { it == null }) return AppResult.Failure(DataError.Serialization)
            return AppResult.Success(questions.filterNotNull())
        }

        private companion object {
            const val TOKEN_ATTEMPTS = 2
        }
    }
