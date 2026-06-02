package voice.features.bookDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import coil.compose.AsyncImage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.filterNotNull
import voice.core.common.rootGraphAs
import voice.core.data.BookId
import voice.core.data.repo.BookRepository
import voice.core.ui.formatTime
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.navigation.Navigator
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface BookDetailGraph {
  val bookRepository: BookRepository
  val navigator: Navigator
}

@ContributesTo(AppScope::class)
interface BookDetailProvider {

  @Provides
  @IntoSet
  fun bookDetailNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.BookDetail> { key ->
    NavEntry(key) {
      BookDetailScreen(bookId = key.bookId)
    }
  }
}

@Composable
fun BookDetailScreen(bookId: BookId) {
  val graph = retain(bookId.value) {
    rootGraphAs<BookDetailGraph>()
  }
  val book = remember(bookId) {
    graph.bookRepository.flow(bookId).filterNotNull()
  }.collectAsState(initial = null).value ?: return

  val content = book.content

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(StringsR.string.book_detail_title)) },
        navigationIcon = {
          IconButton(onClick = { graph.navigator.goBack() }) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = stringResource(StringsR.string.close),
            )
          }
        },
      )
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      content.cover?.let { coverFile ->
        AsyncImage(
          model = coverFile,
          contentDescription = null,
          modifier = Modifier
            .size(200.dp),
        )
        Spacer(Modifier.height(16.dp))
      }

      Text(
        text = content.name,
        style = MaterialTheme.typography.headlineSmall,
      )

      Spacer(Modifier.height(8.dp))

      content.author?.let {
        DetailRow(label = stringResource(StringsR.string.book_detail_author), value = it)
      }
      content.narrator?.let {
        DetailRow(label = stringResource(StringsR.string.book_detail_narrator), value = it)
      }
      content.series?.let { series ->
        val seriesText = if (content.part != null && content.seriesTotal != null) {
          "$series (${content.part} of ${content.seriesTotal})"
        } else if (content.part != null) {
          "$series (${content.part})"
        } else {
          series
        }
        DetailRow(label = stringResource(StringsR.string.book_detail_series), value = seriesText)
      }
      content.genre?.let {
        DetailRow(label = stringResource(StringsR.string.book_detail_genre), value = it)
      }
      content.publisher?.let {
        DetailRow(label = stringResource(StringsR.string.book_detail_publisher), value = it)
      }
      content.publishedDate?.let {
        DetailRow(label = stringResource(StringsR.string.book_detail_published), value = it)
      }
      content.language?.let {
        DetailRow(label = stringResource(StringsR.string.book_detail_language), value = it)
      }

      val totalDuration = book.duration
      DetailRow(
        label = stringResource(StringsR.string.book_detail_duration),
        value = formatTime(totalDuration),
      )
      DetailRow(
        label = stringResource(StringsR.string.book_detail_chapters),
        value = "${book.chapters.size}",
      )

      val progress = if (totalDuration > 0) {
        ((book.position.toFloat() / totalDuration) * 100).toInt()
      } else {
        0
      }
      DetailRow(
        label = stringResource(StringsR.string.book_detail_progress),
        value = "$progress%",
      )

      content.description?.let {
        Spacer(Modifier.height(16.dp))
        Text(
          text = stringResource(StringsR.string.book_detail_description),
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        Text(
          text = it,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}
