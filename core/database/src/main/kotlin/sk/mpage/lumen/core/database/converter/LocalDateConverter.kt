package sk.mpage.lumen.core.database.converter

import androidx.room.TypeConverter
import java.time.LocalDate

internal class LocalDateConverter {
    @TypeConverter
    fun toIsoString(date: LocalDate): String = date.toString()

    @TypeConverter
    fun fromIsoString(iso: String): LocalDate = LocalDate.parse(iso)
}
