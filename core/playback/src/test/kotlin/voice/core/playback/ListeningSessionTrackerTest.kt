package voice.core.playback

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import voice.core.data.BookId
import voice.core.data.ListeningSession
import voice.core.data.repo.internals.dao.ListeningSessionDao
import voice.core.playback.playstate.PlayStateManager

class ListeningSessionTrackerTest {

  private val dao = mockk<ListeningSessionDao> {
    coEvery { insert(any()) } just Runs
  }
  private val playStateManager = PlayStateManager()
  private val playerController = mockk<PlayerController>()

  private fun TestScope.createTracker(): ListeningSessionTracker {
    return ListeningSessionTracker(
      listeningSessionDao = dao,
      playStateManager = playStateManager,
      playerController = playerController,
      scope = backgroundScope,
    )
  }

  @Test
  fun `session is recorded after play and pause with sufficient duration`() = runTest {
    val tracker = createTracker()
    tracker.start()
    tracker.updateCurrentBook(BookId("book1"))

    playStateManager.playState = PlayStateManager.PlayState.Playing
    advanceTimeBy(10_000)
    playStateManager.playState = PlayStateManager.PlayState.Paused
    testScheduler.advanceUntilIdle()

    val sessionSlot = slot<ListeningSession>()
    coVerify { dao.insert(capture(sessionSlot)) }
    sessionSlot.captured.bookId shouldBe BookId("book1")
    (sessionSlot.captured.durationMs >= 5000) shouldBe true
  }

  @Test
  fun `session is not recorded when duration is less than 5 seconds`() = runTest {
    val tracker = createTracker()
    tracker.start()
    tracker.updateCurrentBook(BookId("book1"))

    playStateManager.playState = PlayStateManager.PlayState.Playing
    advanceTimeBy(3_000)
    playStateManager.playState = PlayStateManager.PlayState.Paused
    testScheduler.advanceUntilIdle()

    coVerify(exactly = 0) { dao.insert(any()) }
  }

  @Test
  fun `session is not recorded when no book is set`() = runTest {
    val tracker = createTracker()
    tracker.start()

    playStateManager.playState = PlayStateManager.PlayState.Playing
    advanceTimeBy(10_000)
    playStateManager.playState = PlayStateManager.PlayState.Paused
    testScheduler.advanceUntilIdle()

    coVerify(exactly = 0) { dao.insert(any()) }
  }

  @Test
  fun `flush records session if playing`() = runTest {
    val tracker = createTracker()
    tracker.start()
    tracker.updateCurrentBook(BookId("book1"))

    playStateManager.playState = PlayStateManager.PlayState.Playing
    advanceTimeBy(10_000)
    tracker.flush()

    coVerify { dao.insert(any()) }
  }

  @Test
  fun `flush does nothing when not playing`() = runTest {
    val tracker = createTracker()
    tracker.start()
    tracker.updateCurrentBook(BookId("book1"))

    tracker.flush()

    coVerify(exactly = 0) { dao.insert(any()) }
  }
}
