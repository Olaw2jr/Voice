package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import voice.core.data.ListeningSession

@Dao
public interface ListeningSessionDao {

  @Insert
  public suspend fun insert(session: ListeningSession)

  @Query("SELECT COALESCE(SUM(durationMs), 0) FROM listeningSession WHERE date = :date")
  public suspend fun totalDurationForDate(date: String): Long

  @Query("SELECT COALESCE(SUM(durationMs), 0) FROM listeningSession WHERE date >= :startDate AND date <= :endDate")
  public suspend fun totalDurationBetween(startDate: String, endDate: String): Long

  @Query("SELECT COALESCE(SUM(durationMs), 0) FROM listeningSession")
  public suspend fun totalDurationAllTime(): Long

  @Query("SELECT DISTINCT date FROM listeningSession WHERE durationMs >= :minDurationMs ORDER BY date DESC")
  public suspend fun datesWithMinDuration(minDurationMs: Long): List<String>

  @Query("SELECT COUNT(DISTINCT bookId) FROM listeningSession")
  public suspend fun distinctBooksListened(): Int
}
