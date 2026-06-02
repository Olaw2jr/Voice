package voice.features.bookOverview

import io.kotest.matchers.shouldBe
import org.junit.Test
import voice.features.bookOverview.overview.BookOverviewCategory
import voice.features.bookOverview.overview.category
import java.time.Instant

class BookOverviewCategoryTest {

  @Test
  fun finished() {
    val book = book().let { book ->
      val lastChapter = book.chapters.last()
      book.copy(
        content = book.content.copy(
          currentChapter = lastChapter.id,
          positionInChapter = lastChapter.duration,
        ),
      )
    }
    book.category shouldBe BookOverviewCategory.FINISHED
  }

  @Test
  fun notStarted() {
    val book = book().let { book ->
      val firstChapter = book.chapters.first()
      book.copy(
        content = book.content.copy(
          currentChapter = firstChapter.id,
          positionInChapter = 0,
        ),
      )
    }
    book.category shouldBe BookOverviewCategory.NOT_STARTED
  }

  @Test
  fun current() {
    val book = book().let { book ->
      book.copy(
        content = book.content.copy(
          currentChapter = book.chapters.last().id,
          positionInChapter = 0,
        ),
      )
    }
    book.category shouldBe BookOverviewCategory.CURRENT
  }

  @Test
  fun `finished via completedAt even when position is not at end`() {
    val book = book().let { book ->
      book.copy(
        content = book.content.copy(
          currentChapter = book.chapters.first().id,
          positionInChapter = 100,
          completedAt = Instant.now(),
        ),
      )
    }
    book.category shouldBe BookOverviewCategory.FINISHED
  }

  @Test
  fun `not finished when completedAt is null and position is mid-book`() {
    val book = book().let { book ->
      book.copy(
        content = book.content.copy(
          currentChapter = book.chapters.first().id,
          positionInChapter = 100,
          completedAt = null,
        ),
      )
    }
    book.category shouldBe BookOverviewCategory.CURRENT
  }
}
