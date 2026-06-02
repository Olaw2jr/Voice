package voice.features.bookOverview.overview

import io.kotest.matchers.shouldBe
import org.junit.Test
import voice.core.data.Book
import voice.core.data.BookContent
import voice.core.data.BookId
import voice.core.data.Chapter
import voice.core.data.ChapterId
import voice.core.ui.formatTime
import java.time.Instant
import java.util.UUID

class BookOverviewItemViewStateTest {

  private fun book(
    playbackSpeed: Float = 1F,
    positionInChapter: Long = 0,
    chapterDuration: Long = 10000,
  ): Book {
    val chapter = Chapter(
      id = ChapterId(UUID.randomUUID().toString()),
      name = "Chapter",
      duration = chapterDuration,
      fileLastModified = Instant.EPOCH,
      markData = emptyList(),
      fileSize = 0,
    )
    return Book(
      content = BookContent(
        author = "Author",
        name = "Book",
        positionInChapter = positionInChapter,
        playbackSpeed = playbackSpeed,
        addedAt = Instant.EPOCH,
        chapters = listOf(chapter.id),
        cover = null,
        currentChapter = chapter.id,
        isActive = true,
        lastPlayedAt = Instant.EPOCH,
        skipSilence = false,
        id = BookId(UUID.randomUUID().toString()),
        gain = 0F,
        genre = null,
        narrator = null,
        series = null,
        part = null,
        completedAt = null,
      ),
      chapters = listOf(chapter),
    )
  }

  @Test
  fun `remaining time at 1x speed`() {
    val b = book(playbackSpeed = 1F, positionInChapter = 3000, chapterDuration = 10000)
    val viewState = b.toItemViewState()
    viewState.remainingTime shouldBe formatTime(7000)
  }

  @Test
  fun `remaining time at 2x speed is halved`() {
    val b = book(playbackSpeed = 2F, positionInChapter = 0, chapterDuration = 10000)
    val viewState = b.toItemViewState()
    viewState.remainingTime shouldBe formatTime(5000)
  }

  @Test
  fun `remaining time at half speed is doubled`() {
    val b = book(playbackSpeed = 0.5F, positionInChapter = 0, chapterDuration = 10000)
    val viewState = b.toItemViewState()
    viewState.remainingTime shouldBe formatTime(20000)
  }

  @Test
  fun `progress is 0 at start`() {
    val b = book(positionInChapter = 0, chapterDuration = 10000)
    val viewState = b.toItemViewState()
    viewState.progress shouldBe 0F
  }

  @Test
  fun `progress is 1 at end`() {
    val b = book(positionInChapter = 10000, chapterDuration = 10000)
    val viewState = b.toItemViewState()
    viewState.progress shouldBe 1F
  }

  @Test
  fun `remaining time does not go negative`() {
    val b = book(positionInChapter = 15000, chapterDuration = 10000)
    val viewState = b.toItemViewState()
    viewState.remainingTime shouldBe formatTime(0)
  }
}
