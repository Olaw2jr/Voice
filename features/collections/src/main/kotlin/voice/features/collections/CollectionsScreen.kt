package voice.features.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.launch
import voice.core.common.rootGraphAs
import voice.core.data.Collection
import voice.core.data.repo.internals.dao.CollectionDao
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.navigation.Navigator
import java.time.Instant
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface CollectionsGraph {
  val collectionDao: CollectionDao
  val navigator: Navigator
}

@ContributesTo(AppScope::class)
interface CollectionsProvider {

  @Provides
  @IntoSet
  fun collectionsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Collections> { key ->
    NavEntry(key) {
      CollectionsScreen()
    }
  }
}

@Composable
fun CollectionsScreen() {
  val graph = rootGraphAs<CollectionsGraph>()
  val collections by remember { graph.collectionDao.allCollections() }
    .collectAsState(initial = emptyList())
  val scope = rememberCoroutineScope()
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(StringsR.string.collections_title)) },
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
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
      ) {
        Icon(Icons.Default.Add, contentDescription = stringResource(StringsR.string.add))
      }
    },
  ) { paddingValues ->
    LazyColumn(contentPadding = paddingValues) {
      items(collections, key = { it.id.value }) { collection ->
        ListItem(
          modifier = Modifier.clickable {
            graph.navigator.goTo(Destination.CollectionDetail(collection.id.value))
          },
          headlineContent = { Text(collection.name) },
          trailingContent = {
            IconButton(onClick = {
              scope.launch {
                graph.collectionDao.deleteCollection(collection.id)
              }
            }) {
              Icon(Icons.Outlined.Delete, contentDescription = stringResource(StringsR.string.delete))
            }
          },
        )
      }
      item {
        Spacer(Modifier.size(88.dp))
      }
    }
  }

  if (showAddDialog) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text(stringResource(StringsR.string.collections_add_title)) },
      text = {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(stringResource(StringsR.string.collections_name_hint)) },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              if (name.isNotBlank()) {
                scope.launch {
                  graph.collectionDao.insertCollection(
                    Collection(
                      id = Collection.Id.random(),
                      name = name.trim(),
                      createdAt = Instant.now(),
                      sortOrder = collections.size,
                    ),
                  )
                }
                showAddDialog = false
              }
            },
          ),
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isNotBlank()) {
              scope.launch {
                graph.collectionDao.insertCollection(
                  Collection(
                    id = Collection.Id.random(),
                    name = name.trim(),
                    createdAt = Instant.now(),
                    sortOrder = collections.size,
                  ),
                )
              }
              showAddDialog = false
            }
          },
          enabled = name.isNotBlank(),
        ) {
          Text(stringResource(StringsR.string.dialog_confirm))
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text(stringResource(StringsR.string.dialog_cancel))
        }
      },
    )
  }
}
