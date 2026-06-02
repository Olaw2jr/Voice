package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.Collection
import voice.core.data.CollectionBookCrossRef

@Dao
public interface CollectionDao {

  @Query("SELECT * FROM collection ORDER BY sortOrder ASC")
  public fun allCollections(): Flow<List<Collection>>

  @Query("SELECT * FROM collection ORDER BY sortOrder ASC")
  public suspend fun allCollectionsList(): List<Collection>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insertCollection(collection: Collection)

  @Query("DELETE FROM collection WHERE id = :id")
  public suspend fun deleteCollection(id: Collection.Id)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun addBookToCollection(crossRef: CollectionBookCrossRef)

  @Query("DELETE FROM collectionBookCrossRef WHERE collectionId = :collectionId AND bookId = :bookId")
  public suspend fun removeBookFromCollection(collectionId: String, bookId: BookId)

  @Query("SELECT bookId FROM collectionBookCrossRef WHERE collectionId = :collectionId")
  public fun bookIdsInCollection(collectionId: String): Flow<List<BookId>>

  @Query("SELECT collectionId FROM collectionBookCrossRef WHERE bookId = :bookId")
  public suspend fun collectionsForBook(bookId: BookId): List<String>
}
