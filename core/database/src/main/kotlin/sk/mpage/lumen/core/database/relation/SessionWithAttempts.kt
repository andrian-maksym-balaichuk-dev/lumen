package sk.mpage.lumen.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import sk.mpage.lumen.core.database.entity.QuestionAttemptEntity
import sk.mpage.lumen.core.database.entity.QuizSessionEntity

public data class SessionWithAttempts(
    @Embedded val session: QuizSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "session_id")
    val attempts: List<QuestionAttemptEntity>,
)
