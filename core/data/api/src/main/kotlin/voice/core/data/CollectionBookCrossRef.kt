package voice.core.data

import androidx.room.Entity
import java.time.Instant

@Entity(
  tableName = "collectionBookCrossRef",
  primaryKeys = ["collectionId", "bookId"],
)
public data class CollectionBookCrossRef(
  val collectionId: String,
  val bookId: BookId,
  val addedAt: Instant,
)
