package sk.mpage.lumen.core.database.converter

import androidx.room.TypeConverter
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType

internal class EnumConverters {
    @TypeConverter
    fun gameModeToCode(mode: GameMode): String = mode.code

    @TypeConverter
    fun gameModeFromCode(code: String): GameMode = GameMode.fromCode(code)

    @TypeConverter
    fun difficultyToCode(difficulty: Difficulty): String = difficulty.code

    @TypeConverter
    fun difficultyFromCode(code: String): Difficulty = Difficulty.fromCode(code)

    @TypeConverter
    fun questionTypeToCode(type: QuestionType): String = type.code

    @TypeConverter
    fun questionTypeFromCode(code: String): QuestionType = QuestionType.fromCode(code)

    @TypeConverter
    fun endReasonToCode(reason: EndReason): String = reason.code

    @TypeConverter
    fun endReasonFromCode(code: String): EndReason = EndReason.fromCode(code)
}
