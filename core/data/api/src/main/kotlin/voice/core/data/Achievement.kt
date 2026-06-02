package voice.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "achievement")
public data class Achievement(
  @PrimaryKey
  val id: String,
  val unlockedAt: Instant,
)
