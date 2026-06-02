package voice.core.data.repo.internals

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import voice.core.data.BookId
import voice.core.data.Collection
import voice.core.data.CollectionBookCrossRef
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class CollectionDaoTest {

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

  private val dao get() = db.collectionDao()

  @Test
  fun `insert and retrieve collections`() = runTest {
    val c1 = Collection(
      id = Collection.Id("c1"),
      name = "Favorites",
      createdAt = Instant.now(),
      sortOrder = 0,
    )
    val c2 = Collection(
      id = Collection.Id("c2"),
      name = "Road Trip",
      createdAt = Instant.now(),
      sortOrder = 1,
    )

    dao.insertCollection(c1)
    dao.insertCollection(c2)

    val collections = dao.allCollections().first()
    collections.map { it.name }.shouldContainExactly("Favorites", "Road Trip")
  }

  @Test
  fun `delete collection`() = runTest {
    val c1 = Collection(
      id = Collection.Id("c1"),
      name = "ToDelete",
      createdAt = Instant.now(),
      sortOrder = 0,
    )
    dao.insertCollection(c1)
    dao.deleteCollection(c1.id)

    dao.allCollections().first().shouldBeEmpty()
  }

  @Test
  fun `add and remove book from collection`() = runTest {
    val bookId = BookId("book1")
    dao.insertCollection(
      Collection(id = Collection.Id("c1"), name = "Test", createdAt = Instant.now(), sortOrder = 0),
    )

    dao.addBookToCollection(
      CollectionBookCrossRef(collectionId = "c1", bookId = bookId, addedAt = Instant.now()),
    )

    dao.bookIdsInCollection("c1").first().shouldContainExactly(bookId)

    dao.removeBookFromCollection("c1", bookId)
    dao.bookIdsInCollection("c1").first().shouldBeEmpty()
  }

  @Test
  fun `collectionsForBook returns matching collection ids`() = runTest {
    val bookId = BookId("book1")
    dao.insertCollection(
      Collection(id = Collection.Id("c1"), name = "A", createdAt = Instant.now(), sortOrder = 0),
    )
    dao.insertCollection(
      Collection(id = Collection.Id("c2"), name = "B", createdAt = Instant.now(), sortOrder = 1),
    )

    dao.addBookToCollection(
      CollectionBookCrossRef(collectionId = "c1", bookId = bookId, addedAt = Instant.now()),
    )
    dao.addBookToCollection(
      CollectionBookCrossRef(collectionId = "c2", bookId = bookId, addedAt = Instant.now()),
    )

    val collections = dao.collectionsForBook(bookId)
    collections.toSet() shouldBe setOf("c1", "c2")
  }
}
