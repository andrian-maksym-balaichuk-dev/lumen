package sk.mpage.lumen.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sk.mpage.lumen.core.database.converter.EnumConverters
import sk.mpage.lumen.core.database.converter.InstantConverter
import sk.mpage.lumen.core.database.converter.LocalDateConverter
import sk.mpage.lumen.core.database.converter.StringListConverter
import sk.mpage.lumen.core.database.dao.CategoryDao
import sk.mpage.lumen.core.database.dao.QuizSessionDao
import sk.mpage.lumen.core.database.entity.CategoryEntity
import sk.mpage.lumen.core.database.entity.QuestionAttemptEntity
import sk.mpage.lumen.core.database.entity.QuizSessionEntity

@Database(
    entities = [QuizSessionEntity::class, QuestionAttemptEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class, LocalDateConverter::class, StringListConverter::class, EnumConverters::class)
internal abstract class LumenDatabase : RoomDatabase() {
    abstract fun quizSessionDao(): QuizSessionDao

    abstract fun categoryDao(): CategoryDao
}
