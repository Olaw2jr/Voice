package voice.core.playback

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.ListeningSession
import voice.core.data.repo.internals.dao.ListeningSessionDao
import voice.core.logging.api.Logger
import voice.core.playback.di.PlaybackScope
import voice.core.playback.playstate.PlayStateManager
import java.time.Instant
import java.time.LocalDate

@Inject
@SingleIn(PlaybackScope::class)
class ListeningSessionTracker(
  private val listeningSessionDao: ListeningSessionDao,
  private val playStateManager: PlayStateManager,
  private val playerController: PlayerController,
  private val scope: CoroutineScope,
) {

  private var sessionStart: Instant? = null
  private var sessionBookId: BookId? = null

  fun start() {
    scope.launch {
      playStateManager.flow
        .map { it == PlayStateManager.PlayState.Playing }
        .distinctUntilChanged()
        .collectLatest { playing ->
          if (playing) {
            onPlaybackStarted()
          } else {
            onPlaybackPaused()
          }
        }
    }
  }

  private fun onPlaybackStarted() {
    sessionStart = Instant.now()
    sessionBookId = null
  }

  private suspend fun onPlaybackPaused() {
    val start = sessionStart ?: return
    sessionStart = null
    val durationMs = Instant.now().toEpochMilli() - start.toEpochMilli()
    if (durationMs < 5000) return

    val bookId = sessionBookId ?: return
    val session = ListeningSession.create(
      bookId = bookId,
      startedAt = start,
      durationMs = durationMs,
      date = LocalDate.now().toString(),
    )
    try {
      listeningSessionDao.insert(session)
      Logger.d("Recorded listening session: ${durationMs}ms for $bookId")
    } catch (e: Exception) {
      Logger.w("Failed to record listening session: $e")
    }
  }

  fun updateCurrentBook(bookId: BookId) {
    sessionBookId = bookId
  }

  suspend fun flush() {
    onPlaybackPaused()
  }
}
