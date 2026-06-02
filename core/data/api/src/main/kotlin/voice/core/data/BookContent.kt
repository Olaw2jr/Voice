package voice.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.File
import java.time.Instant

@Entity(tableName = "content2")
public data class BookContent(
  @PrimaryKey
  val id: BookId,
  val playbackSpeed: Float,
  val skipSilence: Boolean,
  val isActive: Boolean,
  val lastPlayedAt: Instant,
  val author: String?,
  val name: String,
  val addedAt: Instant,
  val chapters: List<ChapterId>,
  val currentChapter: ChapterId,
  val positionInChapter: Long,
  val cover: File?,
  @ColumnInfo(defaultValue = "0")
  val gain: Float,
  val genre: String?,
  val narrator: String?,
  val series: String?,
  val part: String?,
  @ColumnInfo(defaultValue = "NULL")
  val completedAt: Instant?,
  @ColumnInfo(defaultValue = "NULL")
  val description: String? = null,
  @ColumnInfo(defaultValue = "NULL")
  val publisher: String? = null,
  @ColumnInfo(defaultValue = "NULL")
  val publishedDate: String? = null,
  @ColumnInfo(defaultValue = "NULL")
  val language: String? = null,
  @ColumnInfo(defaultValue = "NULL")
  val rating: Float? = null,
  @ColumnInfo(defaultValue = "NULL")
  val seriesTotal: Int? = null,
) {

  @Ignore
  val currentChapterIndex: Int = chapters.indexOf(currentChapter)

  init {
    require(currentChapter in chapters && positionInChapter >= 0) {
      "invalid data in $this"
    }
  }
}
