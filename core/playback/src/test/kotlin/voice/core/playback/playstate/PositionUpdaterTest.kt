package voice.core.playback.playstate

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import voice.core.data.Book
import voice.core.data.BookContent
import voice.core.data.BookId
import voice.core.data.Chapter
import voice.core.data.ChapterId
import voice.core.data.repo.BookRepository
import voice.core.featureflag.MemoryFeatureFlag
import voice.core.playback.session.MediaId
import java.time.Instant

class PositionUpdaterTest {

  private val bookId = BookId("book1")
  private val chapter1Id = ChapterId("ch1")
  private val chapter2Id = ChapterId("ch2")
  private val chapter1 = Chapter(
    id = chapter1Id,
    name = "Chapter 1",
    duration = 60000,
    fileLastModified = Instant.EPOCH,
    markData = emptyList(),
    fileSize = 0,
  )
  private val chapter2 = Chapter(
    id = chapter2Id,
    name = "Chapter 2",
    duration = 30000,
    fileLastModified = Instant.EPOCH,
    markData = emptyList(),
    fileSize = 0,
  )

  private fun content(
    currentChapter: ChapterId = chapter1Id,
    positionInChapter: Long = 0,
    completedAt: Instant? = null,
  ) = BookContent(
    id = bookId,
    playbackSpeed = 1F,
    skipSilence = false,
    isActive = true,
    lastPlayedAt = Instant.EPOCH,
    author = null,
    name = "Test",
    addedAt = Instant.EPOCH,
    chapters = listOf(chapter1Id, chapter2Id),
    currentChapter = currentChapter,
    positionInChapter = positionInChapter,
    cover = null,
    gain = 0F,
    genre = null,
    narrator = null,
    series = null,
    part = null,
    completedAt = completedAt,
  )

  private fun book(
    currentChapter: ChapterId = chapter1Id,
    positionInChapter: Long = 0,
    completedAt: Instant? = null,
  ) = Book(
    content = content(currentChapter, positionInChapter, completedAt),
    chapters = listOf(chapter1, chapter2),
  )

  @Test
  fun `completedAt is set when position is near end of last chapter`() = runTest {
    val contentSlot = slot<(BookContent) -> BookContent>()
    val repo = mockk<BookRepository> {
      coEvery { get(bookId) } returns book()
      coEvery { updateBook(bookId, capture(contentSlot)) } returns Unit
    }

    val updater = createUpdater(repo)
    val player = mockPlayer(
      mediaId = MediaId.Chapter(bookId, chapter2Id).toString(),
      position = 26000, // near end of chapter2 (30000 - 4000 < 5000)
    )
    updater.attachTo(player)
    updater.flushPositionNow()

    val updated = contentSlot.captured(content(currentChapter = chapter2Id))
    updated.completedAt.shouldNotBeNull()
  }

  @Test
  fun `completedAt is not set when position is not near end`() = runTest {
    val contentSlot = slot<(BookContent) -> BookContent>()
    val repo = mockk<BookRepository> {
      coEvery { get(bookId) } returns book()
      coEvery { updateBook(bookId, capture(contentSlot)) } returns Unit
    }

    val updater = createUpdater(repo)
    val player = mockPlayer(
      mediaId = MediaId.Chapter(bookId, chapter2Id).toString(),
      position = 10000, // far from end
    )
    updater.attachTo(player)
    updater.flushPositionNow()

    val updated = contentSlot.captured(content(currentChapter = chapter2Id))
    updated.completedAt.shouldBeNull()
  }

  @Test
  fun `completedAt is not set on non-last chapter even near end`() = runTest {
    val contentSlot = slot<(BookContent) -> BookContent>()
    val repo = mockk<BookRepository> {
      coEvery { get(bookId) } returns book()
      coEvery { updateBook(bookId, capture(contentSlot)) } returns Unit
    }

    val updater = createUpdater(repo)
    val player = mockPlayer(
      mediaId = MediaId.Chapter(bookId, chapter1Id).toString(),
      position = 59000, // near end of chapter1, but not last chapter
    )
    updater.attachTo(player)
    updater.flushPositionNow()

    val updated = contentSlot.captured(content(currentChapter = chapter1Id))
    updated.completedAt.shouldBeNull()
  }

  @Test
  fun `completedAt is cleared when rewinding a completed book`() = runTest {
    val completedTime = Instant.now()
    val contentSlot = slot<(BookContent) -> BookContent>()
    val repo = mockk<BookRepository> {
      coEvery { get(bookId) } returns book(completedAt = completedTime)
      coEvery { updateBook(bookId, capture(contentSlot)) } returns Unit
    }

    val updater = createUpdater(repo)
    val player = mockPlayer(
      mediaId = MediaId.Chapter(bookId, chapter1Id).toString(),
      position = 5000, // rewound to early position
    )
    updater.attachTo(player)
    updater.flushPositionNow()

    val updated = contentSlot.captured(
      content(currentChapter = chapter1Id, completedAt = completedTime),
    )
    updated.completedAt.shouldBeNull()
  }

  @Test
  fun `completedAt is preserved when already completed and still near end`() = runTest {
    val completedTime = Instant.now()
    val contentSlot = slot<(BookContent) -> BookContent>()
    val repo = mockk<BookRepository> {
      coEvery { get(bookId) } returns book(completedAt = completedTime)
      coEvery { updateBook(bookId, capture(contentSlot)) } returns Unit
    }

    val updater = createUpdater(repo)
    val player = mockPlayer(
      mediaId = MediaId.Chapter(bookId, chapter2Id).toString(),
      position = 28000, // still near end
    )
    updater.attachTo(player)
    updater.flushPositionNow()

    val updated = contentSlot.captured(
      content(currentChapter = chapter2Id, completedAt = completedTime),
    )
    updated.completedAt shouldBe completedTime
  }

  private fun TestScope.createUpdater(repo: BookRepository): PositionUpdater {
    return PositionUpdater(
      bookRepo = repo,
      scope = backgroundScope,
      playStateManager = PlayStateManager(),
      experimentalPlaybackPersistenceFeatureFlag = MemoryFeatureFlag(false),
    )
  }

  private fun mockPlayer(mediaId: String, position: Long) = mockk<androidx.media3.common.Player> {
    every { currentMediaItem } returns mockk {
      every { getMediaId() } returns mediaId
    }
    every { currentPosition } returns position
    every { addListener(any()) } returns Unit
    every { removeListener(any()) } returns Unit
  }
}
