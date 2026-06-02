package voice.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(tableName = "listeningSession")
public data class ListeningSession(
  @PrimaryKey
  val id: String,
  val bookId: BookId,
  val startedAt: Instant,
  val durationMs: Long,
  val date: String,
) {
  public companion object {
    public fun create(
      bookId: BookId,
      startedAt: Instant,
      durationMs: Long,
      date: String,
    ): ListeningSession = ListeningSession(
      id = UUID.randomUUID().toString(),
      bookId = bookId,
      startedAt = startedAt,
      durationMs = durationMs,
      date = date,
    )
  }
}
