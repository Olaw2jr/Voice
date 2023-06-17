package voice.review

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import voice.data.repo.BookRepository
import voice.playback.playstate.PlayStateManager
import java.time.Clock
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

class ShouldShowReviewDialog
@Inject constructor(
  private val installationTimeProvider: InstallationTimeProvider,
  private val clock: Clock,
  @ReviewDialogShown
  private val reviewDialogShown: DataStore<Boolean>,
  private val bookRepository: BookRepository,
  private val playStateManager: PlayStateManager,
) {

  internal suspend fun shouldShow(): Boolean {
    return isNotPlaying() &&
      enoughTimeElapsedSinceInstallation() &&
      reviewDialogNotShown() &&
      listenedForEnoughTime()
  }

  internal suspend fun setShown() {
    reviewDialogShown.updateData { true }
  }

  private fun enoughTimeElapsedSinceInstallation(): Boolean {
    val timeSinceInstallation = ChronoUnit.MILLIS.between(
      installationTimeProvider.installationTime(),
      clock.instant(),
    ).milliseconds
    return timeSinceInstallation >= 2.days
  }

  private suspend fun reviewDialogNotShown() = !reviewDialogShown.data.first()

  private fun isNotPlaying() = playStateManager.playState != PlayStateManager.PlayState.Playing

  private suspend fun listenedForEnoughTime() = bookRepository.all().sumOf { it.position }.milliseconds >= 1.days
}
