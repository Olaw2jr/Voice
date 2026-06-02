package voice.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(tableName = "collection")
public data class Collection(
  @PrimaryKey
  val id: Id,
  val name: String,
  val createdAt: Instant,
  val sortOrder: Int,
) {

  public data class Id(val value: String) {
    public companion object {
      public fun random(): Id = Id(UUID.randomUUID().toString())
    }
  }
}
