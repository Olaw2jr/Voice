package voice.features.bookmark

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import voice.core.data.Book
import voice.core.data.BookContent
import voice.core.data.BookId
import voice.core.data.Bookmark
import voice.core.data.Chapter
import voice.core.data.ChapterId
import voice.core.data.MarkData
import voice.core.data.repo.BookRepository
import voice.core.data.repo.BookmarkRepo
import voice.core.playback.PlayerController
import voice.core.playback.playstate.PlayStateManager
import voice.navigation.Navigator
import java.time.Instant
import java.util.UUID

class BookmarkViewModelTest {

  private val bookId = BookId("book1")
  private val chapterId = ChapterId("chapter1")
  private val chapter = Chapter(
    id = chapterId,
    name = "Chapter One",
    duration = 60000,
    fileLastModified = Instant.EPOCH,
    markData = listOf(MarkData(startMs = 0, name = "Full Chapter")),
    fileSize = 0,
  )
  private val book = Book(
    content = BookContent(
      id = bookId,
      playbackSpeed = 1F,
      skipSilence = false,
      isActive = true,
      lastPlayedAt = Instant.now(),
      author = "Author",
      name = "Test Book",
      addedAt = Instant.now(),
      chapters = listOf(chapterId),
      currentChapter = chapterId,
      positionInChapter = 5000,
      cover = null,
      gain = 0F,
      genre = null,
      narrator = null,
      series = null,
      part = null,
      completedAt = null,
    ),
    chapters = listOf(chapter),
  )

  private val bookmarkRepo = mockk<BookmarkRepo> {
    coEvery { addBookmark(any()) } just Runs
    coEvery { bookmarks(any()) } returns emptyList()
  }
  private val repo = mockk<BookRepository> {
    coEvery { get(bookId) } returns book
  }

  private fun createViewModel(): BookmarkViewModel {
    return BookmarkViewModel(
      currentBookStore = mockk {
        coEvery { updateData(any()) } returns bookId
      },
      repo = repo,
      bookmarkRepo = bookmarkRepo,
      playStateManager = PlayStateManager(),
      playerController = mockk<PlayerController>(),
      navigator = mockk<Navigator> {
        every { goBack() } just Runs
      },
      context = mockk(relaxed = true),
      bookId = bookId,
    )
  }

  @Test
  fun `editBookmark updates note`() = runTest {
    val bookmarkId = Bookmark.Id.random()
    val original = Bookmark(
      bookId = bookId,
      chapterId = chapterId,
      title = "Original",
      time = 5000,
      addedAt = Instant.now(),
      setBySleepTimer = false,
      id = bookmarkId,
      note = null,
      category = null,
    )

    coEvery { bookmarkRepo.bookmarks(any()) } returns listOf(original)

    val viewModel = createViewModel()
    // Trigger bookmark loading
    viewModel.viewState()

    viewModel.editBookmark(bookmarkId, "Updated Title", "My note")

    val slot = slot<Bookmark>()
    coVerify { bookmarkRepo.addBookmark(capture(slot)) }
    slot.captured.title shouldBe "Updated Title"
    slot.captured.note shouldBe "My note"
    slot.captured.setBySleepTimer shouldBe false
  }

  @Test
  fun `editBookmark clears blank note`() = runTest {
    val bookmarkId = Bookmark.Id.random()
    val original = Bookmark(
      bookId = bookId,
      chapterId = chapterId,
      title = "Title",
      time = 5000,
      addedAt = Instant.now(),
      setBySleepTimer = false,
      id = bookmarkId,
      note = "Old note",
      category = null,
    )

    coEvery { bookmarkRepo.bookmarks(any()) } returns listOf(original)

    val viewModel = createViewModel()
    viewModel.viewState()

    viewModel.editBookmark(bookmarkId, "Title", "   ")

    val slot = slot<Bookmark>()
    coVerify { bookmarkRepo.addBookmark(capture(slot)) }
    slot.captured.note shouldBe null
  }

  @Test
  fun `exportBookmarks formats output with title and note`() = runTest {
    val bookmark = Bookmark(
      bookId = bookId,
      chapterId = chapterId,
      title = "Important passage",
      time = 30000,
      addedAt = Instant.now(),
      setBySleepTimer = false,
      id = Bookmark.Id.random(),
      note = "Remember this part",
      category = null,
    )

    coEvery { bookmarkRepo.bookmarks(any()) } returns listOf(bookmark)

    val viewModel = createViewModel()
    viewModel.viewState()

    val exported = viewModel.exportBookmarks()
    exported shouldContain "Bookmarks"
    exported shouldContain "Title: Important passage"
    exported shouldContain "Note: Remember this part"
  }

  @Test
  fun `exportBookmarks omits null title and note`() = runTest {
    val bookmark = Bookmark(
      bookId = bookId,
      chapterId = chapterId,
      title = null,
      time = 10000,
      addedAt = Instant.now(),
      setBySleepTimer = false,
      id = Bookmark.Id.random(),
      note = null,
      category = null,
    )

    coEvery { bookmarkRepo.bookmarks(any()) } returns listOf(bookmark)

    val viewModel = createViewModel()
    viewModel.viewState()

    val exported = viewModel.exportBookmarks()
    exported shouldContain "Bookmarks"
    exported shouldNotContain "Title:"
    exported shouldNotContain "Note:"
  }
}
