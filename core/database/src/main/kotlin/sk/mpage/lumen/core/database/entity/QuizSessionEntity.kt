package sk.mpage.lumen.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "quiz_session",
    indices = [
        Index(value = ["game_uuid"], unique = true),
        Index("finished_at"),
        Index(value = ["category_id", "finished_at"]),
        Index(value = ["mode", "correct_count"]),
        Index("score_percent"),
        Index("duration_seconds"),
        Index("local_date"),
    ],
)
public data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "game_uuid") val gameUuid: String,
    val mode: GameMode,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
    @ColumnInfo(name = "category_name") val categoryName: String,
    val difficulty: Difficulty,
    @ColumnInfo(name = "question_type") val questionType: QuestionType,
    @ColumnInfo(name = "started_at") val startedAt: Instant,
    @ColumnInfo(name = "finished_at") val finishedAt: Instant,
    @ColumnInfo(name = "local_date") val localDate: LocalDate,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Long,
    @ColumnInfo(name = "answer_time_ms") val answerTimeMs: Long,
    @ColumnInfo(name = "total_questions") val totalQuestions: Int,
    @ColumnInfo(name = "correct_count") val correctCount: Int,
    @ColumnInfo(name = "wrong_count") val wrongCount: Int,
    @ColumnInfo(name = "timeout_count") val timeoutCount: Int,
    @ColumnInfo(name = "score_percent") val scorePercent: Int,
    @ColumnInfo(name = "avg_answer_ms") val avgAnswerMs: Long,
    @ColumnInfo(name = "end_reason") val endReason: EndReason,
)
