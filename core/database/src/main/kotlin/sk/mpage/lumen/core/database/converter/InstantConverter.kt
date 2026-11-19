package sk.mpage.lumen.core.database.converter

import androidx.room.TypeConverter
import java.time.Instant

internal class InstantConverter {
    @TypeConverter
    fun toEpochMillis(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun fromEpochMillis(epochMillis: Long): Instant = Instant.ofEpochMilli(epochMillis)
}
