package voice.core.data.repo.internals

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import voice.core.data.BookId
import voice.core.data.ListeningSession

@RunWith(AndroidJUnit4::class)
class ListeningSessionDaoTest {

  private lateinit var db: AppDb

  @Before
  fun setup() {
    db = Room.inMemoryDatabaseBuilder(
      ApplicationProvider.getApplicationContext(),
      AppDb::class.java,
    ).build()
  }

  @After
  fun teardown() {
    db.close()
  }

  private val dao get() = db.listeningSessionDao()

  @Test
  fun `totalDurationForDate sums sessions on same date`() = runTest {
    dao.insert(session(date = "2026-06-01", durationMs = 10000))
    dao.insert(session(date = "2026-06-01", durationMs = 20000))
    dao.insert(session(date = "2026-06-02", durationMs = 5000))

    dao.totalDurationForDate("2026-06-01") shouldBe 30000
    dao.totalDurationForDate("2026-06-02") shouldBe 5000
    dao.totalDurationForDate("2026-06-03") shouldBe 0
  }

  @Test
  fun `totalDurationBetween sums over date range`() = runTest {
    dao.insert(session(date = "2026-06-01", durationMs = 10000))
    dao.insert(session(date = "2026-06-03", durationMs = 20000))
    dao.insert(session(date = "2026-06-05", durationMs = 30000))

    dao.totalDurationBetween("2026-06-01", "2026-06-03") shouldBe 30000
    dao.totalDurationBetween("2026-06-01", "2026-06-05") shouldBe 60000
  }

  @Test
  fun `totalDurationAllTime sums all sessions`() = runTest {
    dao.insert(session(durationMs = 100))
    dao.insert(session(durationMs = 200))
    dao.insert(session(durationMs = 300))

    dao.totalDurationAllTime() shouldBe 600
  }

  @Test
  fun `datesWithMinDuration filters by threshold`() = runTest {
    dao.insert(session(date = "2026-06-01", durationMs = 400000))
    dao.insert(session(date = "2026-06-02", durationMs = 100))
    dao.insert(session(date = "2026-06-03", durationMs = 500000))

    val dates = dao.datesWithMinDuration(300000)
    dates.shouldContainExactly("2026-06-03", "2026-06-01")
  }

  private fun session(
    date: String = "2026-06-01",
    durationMs: Long = 10000,
    bookId: BookId = BookId("book1"),
  ) = ListeningSession.create(
    bookId = bookId,
    startedAt = java.time.Instant.now(),
    durationMs = durationMs,
    date = date,
  )
}
