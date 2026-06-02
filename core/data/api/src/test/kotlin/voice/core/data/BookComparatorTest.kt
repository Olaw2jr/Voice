package voice.core.data

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.Test

class BookComparatorTest {

  private val b1 = book(name = "A", lastPlayedAtMillis = 0, addedAtMillis = 2)
  private val b2 = book(name = "B", lastPlayedAtMillis = 0, addedAtMillis = 2)
  private val b3 = book(name = "B", lastPlayedAtMillis = 2, addedAtMillis = 1)
  private val b4 = book(name = "D", lastPlayedAtMillis = 5, addedAtMillis = 7)
  private val b5 = book(name = "C", lastPlayedAtMillis = 5, addedAtMillis = 6)
  private val books = listOf(b1, b2, b3, b4, b5)

  @Test
  fun byLastPlayed() {
    val sorted = books.sortedWith(BookComparator.ByLastPlayed)
    sorted.shouldContainExactly(b4, b5, b3, b1, b2)
  }

  @Test
  fun byName() {
    val sorted = books.sortedWith(BookComparator.ByName)
    sorted.shouldContainExactly(b1, b2, b3, b5, b4)
  }

  @Test
  fun byDateAdded() {
    val sorted = books.sortedWith(BookComparator.ByDateAdded)
    sorted.shouldContainExactly(b4, b5, b1, b2, b3)
  }

  @Test
  fun byAuthor() {
    val a = book(name = "X").let { it.copy(content = it.content.copy(author = "Alice")) }
    val b = book(name = "Y").let { it.copy(content = it.content.copy(author = "Bob")) }
    val c = book(name = "Z").let { it.copy(content = it.content.copy(author = null)) }
    val sorted = listOf(b, c, a).sortedWith(BookComparator.ByAuthor)
    sorted.shouldContainExactly(c, a, b)
  }
}
