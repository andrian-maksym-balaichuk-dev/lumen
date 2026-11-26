package sk.mpage.lumen.core.testing.repository

import kotlinx.coroutines.CoroutineScope
import sk.mpage.lumen.core.domain.game.QuestionFeed
import sk.mpage.lumen.core.domain.repository.QuestionRepository
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.testing.feed.FakeQuestionFeed

public class FakeQuestionRepository(
    public val feed: FakeQuestionFeed,
) : QuestionRepository {
    override fun feed(
        config: QuizConfig,
        scope: CoroutineScope,
    ): QuestionFeed = feed
}
