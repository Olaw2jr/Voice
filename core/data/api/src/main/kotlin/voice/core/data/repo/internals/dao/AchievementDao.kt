package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import voice.core.data.Achievement

@Dao
public interface AchievementDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  public suspend fun insert(achievement: Achievement)

  @Query("SELECT * FROM achievement ORDER BY unlockedAt DESC")
  public suspend fun allAchievements(): List<Achievement>

  @Query("SELECT COUNT(*) FROM achievement WHERE id = :id")
  public suspend fun hasAchievement(id: String): Boolean
}
