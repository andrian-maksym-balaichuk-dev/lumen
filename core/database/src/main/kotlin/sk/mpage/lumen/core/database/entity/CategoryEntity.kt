package sk.mpage.lumen.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "category", indices = [Index("name")])
public data class CategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
