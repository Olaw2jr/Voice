package voice.features.bookOverview.editBookCategory

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import voice.core.data.BookContent
import voice.core.data.repo.BookRepository
import voice.features.bookOverview.book
import voice.features.bookOverview.bottomSheet.BottomSheetItem

class EditBookCategoryViewModelTest {

  @Test
  fun `mark as completed sets completedAt`() = runTest {
    val book = book()
    val updateSlot = slot<(BookContent) -> BookContent>()
    val repo = mockk<BookRepository> {
      coEvery { get(book.id) } returns book
      coEvery { updateBook(book.id, capture(updateSlot)) } returns Unit
    }

    val viewModel = EditBookCategoryViewModel(repo)
    viewModel.onItemClick(book.id, BottomSheetItem.BookCategoryMarkAsCompleted)

    val updated = updateSlot.captured(book.content)
    updated.completedAt.shouldNotBeNull()
    updated.currentChapter shouldBe book.chapters.last().id
    updated.positionInChapter shouldBe book.chapters.last().duration
  }

  @Test
  fun `mark as current clears completedAt`() = runTest {
    val book = book()
    val updateSlot = slot<(BookContent) -> BookContent>()
    val repo = mockk<BookRepository> {
      coEvery { get(book.id) } returns book
      coEvery { updateBook(book.id, capture(updateSlot)) } returns Unit
    }

    val viewModel = EditBookCategoryViewModel(repo)
    viewModel.onItemClick(book.id, BottomSheetItem.BookCategoryMarkAsCurrent)

    val updated = updateSlot.captured(book.content)
    updated.completedAt.shouldBeNull()
    updated.positionInChapter shouldBe 1L
  }

  @Test
  fun `mark as not started clears completedAt`() = runTest {
    val book = book()
    val updateSlot = slot<(BookContent) -> BookContent>()
    val repo = mockk<BookRepository> {
      coEvery { get(book.id) } returns book
      coEvery { updateBook(book.id, capture(updateSlot)) } returns Unit
    }

    val viewModel = EditBookCategoryViewModel(repo)
    viewModel.onItemClick(book.id, BottomSheetItem.BookCategoryMarkAsNotStarted)

    val updated = updateSlot.captured(book.content)
    updated.completedAt.shouldBeNull()
    updated.positionInChapter shouldBe 0L
  }
}
