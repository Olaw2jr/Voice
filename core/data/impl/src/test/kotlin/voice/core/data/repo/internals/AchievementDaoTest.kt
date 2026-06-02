package voice.core.data.repo.internals

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import voice.core.data.Achievement
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class AchievementDaoTest {

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

  private val dao get() = db.achievementDao()

  @Test
  fun `insert and retrieve achievements`() = runTest {
    dao.allAchievements().shouldBeEmpty()

    dao.insert(Achievement(id = "streak_7", unlockedAt = Instant.now()))
    dao.insert(Achievement(id = "first_book", unlockedAt = Instant.now()))

    dao.allAchievements().size shouldBe 2
  }

  @Test
  fun `insert ignore does not overwrite existing`() = runTest {
    val original = Instant.parse("2026-01-01T00:00:00Z")
    dao.insert(Achievement(id = "streak_7", unlockedAt = original))
    dao.insert(Achievement(id = "streak_7", unlockedAt = Instant.now()))

    val achievements = dao.allAchievements()
    achievements.size shouldBe 1
    achievements.first().unlockedAt shouldBe original
  }

  @Test
  fun `hasAchievement returns correct result`() = runTest {
    dao.hasAchievement("streak_7") shouldBe false

    dao.insert(Achievement(id = "streak_7", unlockedAt = Instant.now()))

    dao.hasAchievement("streak_7") shouldBe true
    dao.hasAchievement("streak_30") shouldBe false
  }
}
