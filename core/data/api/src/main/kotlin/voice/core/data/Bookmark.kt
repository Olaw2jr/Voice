package voice.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(tableName = "bookmark2")
public data class Bookmark(
  val bookId: BookId,
  val chapterId: ChapterId,
  val title: String?,
  val time: Long,
  val addedAt: Instant,
  val setBySleepTimer: Boolean,
  @PrimaryKey
  val id: Id,
  @ColumnInfo(defaultValue = "NULL")
  val note: String? = null,
  @ColumnInfo(defaultValue = "NULL")
  val category: String? = null,
) {

  public data class Id(val value: UUID) {
    public companion object {
      public fun random(): Id = Id(UUID.randomUUID())
    }
  }
}
