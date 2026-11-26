package sk.mpage.lumen.core.domain.repository

import kotlinx.coroutines.CoroutineScope
import sk.mpage.lumen.core.domain.game.QuestionFeed
import sk.mpage.lumen.core.model.QuizConfig

public interface QuestionRepository {
    public fun feed(
        config: QuizConfig,
        scope: CoroutineScope,
    ): QuestionFeed
}
