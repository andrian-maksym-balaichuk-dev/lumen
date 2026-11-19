package sk.mpage.lumen.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal class StringListConverter {
    @TypeConverter
    fun toJson(values: List<String>): String = Json.encodeToString(serializer, values)

    @TypeConverter
    fun fromJson(json: String): List<String> = Json.decodeFromString(serializer, json)

    private companion object {
        val serializer = ListSerializer(String.serializer())
    }
}
