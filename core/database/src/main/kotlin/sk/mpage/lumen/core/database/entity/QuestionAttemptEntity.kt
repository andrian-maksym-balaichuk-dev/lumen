package sk.mpage.lumen.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.QuestionType
import java.time.Instant

@Entity(
    tableName = "question_attempt",
    foreignKeys = [
        ForeignKey(
            entity = QuizSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["session_id", "position"], unique = true),
    ],
)
public data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: Long = 0,
    val position: Int,
    @ColumnInfo(name = "question_text") val questionText: String,
    @ColumnInfo(name = "category_name") val categoryName: String,
    val difficulty: Difficulty,
    val type: QuestionType,
    val options: List<String>,
    @ColumnInfo(name = "correct_answer") val correctAnswer: String,
    @ColumnInfo(name = "selected_answer") val selectedAnswer: String?,
    @ColumnInfo(name = "is_correct") val isCorrect: Boolean,
    @ColumnInfo(name = "time_spent_ms") val timeSpentMs: Long,
    @ColumnInfo(name = "answered_at") val answeredAt: Instant,
)
