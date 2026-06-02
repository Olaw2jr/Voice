package voice.features.bookOverview.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.launch
import voice.core.common.rootGraphAs
import voice.core.data.BookId
import voice.core.ui.PlayButton
import voice.core.ui.VoiceTheme
import voice.features.bookOverview.bottomSheet.BottomSheetContent
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.deleteBook.DeleteBookDialog
import voice.features.bookOverview.di.BookOverviewGraph
import voice.features.bookOverview.editTitle.EditBookTitleDialog
import voice.features.bookOverview.overview.BookOverviewCategory
import voice.features.bookOverview.overview.BookOverviewItemViewState
import voice.features.bookOverview.overview.BookOverviewLayoutMode
import voice.features.bookOverview.overview.BookOverviewViewState
import voice.features.bookOverview.search.BookSearchViewState
import voice.features.bookOverview.views.topbar.BookOverviewTopBar
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import java.util.UUID
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface BookOverviewProvider {

  @Provides
  @IntoSet
  fun bookOverviewNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.BookOverview> { key ->
    NavEntry(key) {
      BookOverviewScreen()
    }
  }
}

@Composable
fun BookOverviewScreen(modifier: Modifier = Modifier) {
  val bookGraph = retain<BookOverviewGraph> {
    rootGraphAs<BookOverviewGraph.Factory.Provider>()
      .bookOverviewGraphProviderFactory.create()
  }
  val bookOverviewViewModel = bookGraph.bookOverviewViewModel
  val editBookTitleViewModel = bookGraph.editBookTitleViewModel
  val bottomSheetViewModel = bookGraph.bottomSheetViewModel
  val deleteBookViewModel = bookGraph.deleteBookViewModel
  val fileCoverViewModel = bookGraph.fileCoverViewModel

  LaunchedEffect(Unit) {
    bookOverviewViewModel.attach()
  }
  val viewState = bookOverviewViewModel.state()

  val scope = rememberCoroutineScope()

  val getContentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri ->
      if (uri != null) {
        fileCoverViewModel.onImagePicked(uri)
      }
    },
  )

  var showBottomSheet by remember { mutableStateOf(false) }
  BookOverview(
    viewState = viewState,
    onSettingsClick = bookOverviewViewModel::onSettingsClick,
    onBookClick = bookOverviewViewModel::onBookClick,
    onBookLongClick = { bookId ->
      bottomSheetViewModel.bookSelected(bookId)
      showBottomSheet = true
    },
    onBookFolderClick = bookOverviewViewModel::onBookFolderClick,
    onPlayButtonClick = bookOverviewViewModel::playPause,
    onSearchActiveChange = bookOverviewViewModel::onSearchActiveChange,
    onSearchQueryChange = bookOverviewViewModel::onSearchQueryChange,
    onSearchBookClick = bookOverviewViewModel::onSearchBookClick,
    onPermissionBugCardClick = bookOverviewViewModel::onPermissionBugCardClick,
    onSortOrderChange = bookOverviewViewModel::onSortOrderChange,
    onStatusFilterChange = bookOverviewViewModel::onStatusFilterChange,
  )
  val deleteBookViewState = deleteBookViewModel.state.value
  if (deleteBookViewState != null) {
    DeleteBookDialog(
      viewState = deleteBookViewState,
      onDismiss = deleteBookViewModel::onDismiss,
      onConfirmDeletion = deleteBookViewModel::onConfirmDeletion,
      onDeleteCheckBoxCheck = deleteBookViewModel::onDeleteCheckBoxCheck,
    )
  }
  val editBookTitleState = editBookTitleViewModel.state.value
  if (editBookTitleState != null) {
    EditBookTitleDialog(
      onDismissEditTitleClick = editBookTitleViewModel::onDismissEditTitle,
      onConfirmEditTitle = editBookTitleViewModel::onConfirmEditTitle,
      viewState = editBookTitleState,
      onUpdateEditTitle = editBookTitleViewModel::onUpdateEditTitle,
    )
  }

  if (showBottomSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      modifier = modifier,
      sheetState = sheetState,
      content = {
        BottomSheetContent(
          state = bottomSheetViewModel.state.value,
          onItemClick = { item ->
            if (item == BottomSheetItem.FileCover) {
              getContentLauncher.launch("image/*")
            }
            scope.launch {
              sheetState.hide()
              bottomSheetViewModel.onItemClick(item)
              showBottomSheet = false
            }
          },
        )
      },
      onDismissRequest = {
        showBottomSheet = false
      },
    )
  }
}

@Composable
internal fun BookOverview(
  viewState: BookOverviewViewState,
  onSettingsClick: () -> Unit,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  onBookFolderClick: () -> Unit,
  onPlayButtonClick: () -> Unit,
  onSearchActiveChange: (Boolean) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onSearchBookClick: (BookId) -> Unit,
  onPermissionBugCardClick: () -> Unit,
  onSortOrderChange: (String) -> Unit,
  onStatusFilterChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      BookOverviewTopBar(
        viewState = viewState,
        onBookFolderClick = onBookFolderClick,
        onSettingsClick = onSettingsClick,
        onActiveChange = onSearchActiveChange,
        onQueryChange = onSearchQueryChange,
        onSearchBookClick = onSearchBookClick,
      )
    },
    floatingActionButton = {
      if (viewState.playButtonState != null) {
        PlayButton(
          modifier = Modifier.navigationBarsPadding(),
          playing = viewState.playButtonState == BookOverviewViewState.PlayButtonState.Playing,
          fabSize = 56.dp,
          iconSize = 24.dp,
          onPlayClick = onPlayButtonClick,
        )
      }
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) { contentPadding ->
    Column(
      Modifier
        .padding(contentPadding)
        .consumeWindowInsets(contentPadding),
    ) {
      if (viewState.books.isNotEmpty()) {
        FilterSortBar(
          sortOrder = viewState.sortOrder,
          statusFilter = viewState.statusFilter,
          onSortOrderChange = onSortOrderChange,
          onStatusFilterChange = onStatusFilterChange,
        )
      }
      Box(modifier = Modifier.weight(1f)) {
        when (viewState.layoutMode) {
          BookOverviewLayoutMode.List -> {
            ListBooks(
              books = viewState.books,
              onBookClick = onBookClick,
              onBookLongClick = onBookLongClick,
              showPermissionBugCard = viewState.showStoragePermissionBugCard,
              onPermissionBugCardClick = onPermissionBugCardClick,
            )
          }
          BookOverviewLayoutMode.Grid -> {
            GridBooks(
              books = viewState.books,
              onBookClick = onBookClick,
              onBookLongClick = onBookLongClick,
              showPermissionBugCard = viewState.showStoragePermissionBugCard,
              onPermissionBugCardClick = onPermissionBugCardClick,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSortBar(
  sortOrder: String,
  statusFilter: String,
  onSortOrderChange: (String) -> Unit,
  onStatusFilterChange: (String) -> Unit,
) {
  var showSortMenu by remember { mutableStateOf(false) }
  FlowRow(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    val filters = listOf(
      "ALL" to stringResource(StringsR.string.filter_all),
      "CURRENT" to stringResource(StringsR.string.book_header_current),
      "NOT_STARTED" to stringResource(StringsR.string.book_header_not_started),
      "FINISHED" to stringResource(StringsR.string.book_header_completed),
    )
    filters.forEach { (value, label) ->
      FilterChip(
        selected = statusFilter == value,
        onClick = { onStatusFilterChange(value) },
        label = { Text(label) },
      )
    }

    Box {
      IconButton(onClick = { showSortMenu = true }) {
        Icon(Icons.Outlined.Sort, contentDescription = stringResource(StringsR.string.sort_by))
      }
      DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false },
      ) {
        val sortOptions = listOf(
          "LAST_PLAYED" to stringResource(StringsR.string.sort_last_played),
          "TITLE" to stringResource(StringsR.string.sort_title),
          "AUTHOR" to stringResource(StringsR.string.sort_author),
          "DATE_ADDED" to stringResource(StringsR.string.sort_date_added),
        )
        sortOptions.forEach { (value, label) ->
          DropdownMenuItem(
            text = { Text(label) },
            onClick = {
              onSortOrderChange(value)
              showSortMenu = false
            },
          )
        }
      }
    }
  }
}

@Suppress("ktlint:compose:preview-public-check")
@Preview
@Composable
fun BookOverviewPreview(
  @PreviewParameter(BookOverviewPreviewParameterProvider::class)
  viewState: BookOverviewViewState,
) {
  VoiceTheme {
    BookOverview(
      viewState = viewState,
      onSettingsClick = {},
      onBookClick = {},
      onBookLongClick = {},
      onBookFolderClick = {},
      onPlayButtonClick = {},
      onSearchActiveChange = {},
      onSearchQueryChange = {},
      onSearchBookClick = {},
      onPermissionBugCardClick = {},
      onSortOrderChange = {},
      onStatusFilterChange = {},
    )
  }
}

internal class BookOverviewPreviewParameterProvider : PreviewParameterProvider<BookOverviewViewState> {

  fun book(): BookOverviewItemViewState {
    return BookOverviewItemViewState(
      name = "Book",
      author = "Author",
      cover = null,
      progress = 0.8F,
      id = BookId(UUID.randomUUID().toString()),
      remainingTime = "01:04",
    )
  }

  override val values = sequenceOf(
    BookOverviewViewState(
      books = mapOf(
        BookOverviewCategory.CURRENT to buildMap {
          repeat(10) {
            put(
              BookId(UUID.randomUUID().toString()),
              mutableStateOf(book()),
            )
          }
        },
        BookOverviewCategory.FINISHED to buildMap {
          repeat(2) {
            put(
              BookId(UUID.randomUUID().toString()),
              mutableStateOf(book()),
            )
          }
        },
      ),
      layoutMode = BookOverviewLayoutMode.List,
      playButtonState = BookOverviewViewState.PlayButtonState.Paused,
      showAddBookHint = false,
      showSearchIcon = true,
      isLoading = true,
      searchActive = true,
      searchViewState = BookSearchViewState.EmptySearch(
        suggestedAuthors = emptyList(),
        recentQueries = emptyList(),
        query = "",
      ),
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = true,
      sortOrder = "LAST_PLAYED",
      statusFilter = "ALL",
    ),
  )
}
