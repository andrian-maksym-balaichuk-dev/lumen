package sk.mpage.lumen.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import sk.mpage.lumen.core.database.entity.QuestionAttemptEntity
import sk.mpage.lumen.core.database.entity.QuizSessionEntity
import sk.mpage.lumen.core.database.relation.SessionWithAttempts
import sk.mpage.lumen.core.model.GameMode
import java.time.Instant
import java.time.LocalDate

@Dao
internal abstract class QuizSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertSession(session: QuizSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertAttempts(attempts: List<QuestionAttemptEntity>)

    @Query("SELECT id FROM quiz_session WHERE game_uuid = :gameUuid")
    protected abstract suspend fun findIdByUuid(gameUuid: String): Long?

    @Query("SELECT MAX(correct_count) FROM quiz_session WHERE mode = :mode")
    protected abstract suspend fun maxCorrect(mode: GameMode): Int?

    @Transaction
    open suspend fun insertFinishedGame(
        session: QuizSessionEntity,
        attempts: List<QuestionAttemptEntity>,
    ): InsertOutcome {
        findIdByUuid(session.gameUuid)?.let { return InsertOutcome(it, previousBest = null, alreadySaved = true) }
        val previousBest = if (session.mode == GameMode.MARATHON) maxCorrect(GameMode.MARATHON) else null
        val id = insertSession(session)
        insertAttempts(attempts.map { it.copy(sessionId = id) })
        return InsertOutcome(id, previousBest, alreadySaved = false)
    }

    @Query(
        """
        SELECT * FROM quiz_session
        WHERE (:categoryId IS NULL OR category_id = :categoryId)
          AND (:mode IS NULL OR mode = :mode)
        ORDER BY finished_at DESC, id DESC
        """,
    )
    abstract fun observeNewest(
        categoryId: Int?,
        mode: GameMode?,
    ): Flow<List<QuizSessionEntity>>

    @Query(
        """
        SELECT * FROM quiz_session
        WHERE (:categoryId IS NULL OR category_id = :categoryId)
          AND (:mode IS NULL OR mode = :mode)
        ORDER BY score_percent DESC, correct_count DESC, duration_seconds ASC, id DESC
        """,
    )
    abstract fun observeBestScore(
        categoryId: Int?,
        mode: GameMode?,
    ): Flow<List<QuizSessionEntity>>

    @Query(
        """
        SELECT * FROM quiz_session
        WHERE (:categoryId IS NULL OR category_id = :categoryId)
          AND (:mode IS NULL OR mode = :mode)
        ORDER BY duration_seconds ASC, score_percent DESC, finished_at DESC, id DESC
        """,
    )
    abstract fun observeFastest(
        categoryId: Int?,
        mode: GameMode?,
    ): Flow<List<QuizSessionEntity>>

    @Transaction
    @Query("SELECT * FROM quiz_session WHERE id = :id")
    abstract fun observeWithAttempts(id: Long): Flow<SessionWithAttempts?>

    @Query("SELECT DISTINCT local_date FROM quiz_session WHERE score_percent >= :minPercent ORDER BY local_date DESC")
    abstract fun observeQualifyingDays(minPercent: Int): Flow<List<LocalDate>>

    @Query("SELECT MAX(correct_count) FROM quiz_session WHERE mode = :mode")
    abstract fun observeMaxCorrect(mode: GameMode): Flow<Int?>

    @Query("SELECT MAX(correct_count) FROM quiz_session WHERE mode = :mode AND id < :sessionId")
    abstract suspend fun maxCorrectBefore(
        mode: GameMode,
        sessionId: Long,
    ): Int?

    @Query("SELECT COUNT(*) FROM quiz_session")
    abstract fun observeCount(): Flow<Int>

    @Query("SELECT MAX(finished_at) FROM quiz_session")
    abstract suspend fun lastFinishedAt(): Instant?

    @Query(
        """
        SELECT DISTINCT category_id AS id, category_name AS name FROM quiz_session
        WHERE category_id IS NOT NULL ORDER BY category_name
        """,
    )
    abstract fun observePlayedCategories(): Flow<List<PlayedCategory>>

    @Query("DELETE FROM quiz_session WHERE id = :id")
    abstract suspend fun delete(id: Long)
}
