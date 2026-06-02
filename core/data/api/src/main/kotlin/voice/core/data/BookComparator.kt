package voice.core.data

import voice.core.common.comparator.NaturalOrderComparator

public enum class BookComparator(private val comparatorFunction: Comparator<Book>) : Comparator<Book> by comparatorFunction {

  ByLastPlayed(
    compareByDescending {
      it.content.lastPlayedAt
    },
  ),
  ByName(
    Comparator { left, right ->
      NaturalOrderComparator.stringComparator.compare(left.content.name, right.content.name)
    },
  ),
  ByDateAdded(
    compareByDescending {
      it.content.addedAt
    },
  ),
  ByAuthor(
    Comparator { left, right ->
      val leftAuthor = left.content.author ?: ""
      val rightAuthor = right.content.author ?: ""
      NaturalOrderComparator.stringComparator.compare(leftAuthor, rightAuthor)
    },
  ),
}
