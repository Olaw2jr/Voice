package voice.features.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.data.repo.internals.dao.AchievementDao
import voice.core.data.repo.internals.dao.ListeningSessionDao
import voice.core.ui.formatTime
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.navigation.Navigator
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface StatsGraph {
  val listeningSessionDao: ListeningSessionDao
  val achievementDao: AchievementDao
  val navigator: Navigator
}

@ContributesTo(AppScope::class)
interface StatsProvider {

  @Provides
  @IntoSet
  fun statsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Statistics> { key ->
    NavEntry(key) {
      StatisticsScreen()
    }
  }
}

data class StatsViewState(
  val todayMs: Long = 0,
  val weekMs: Long = 0,
  val monthMs: Long = 0,
  val allTimeMs: Long = 0,
  val currentStreak: Int = 0,
  val level: ListeningLevel = ListeningLevel.NEWBIE,
  val achievementCount: Int = 0,
)

enum class ListeningLevel(val label: String, val thresholdHours: Long) {
  NEWBIE("Newbie", 0),
  NOVICE("Novice", 10),
  PRO("Pro", 50),
  SCHOLAR("Scholar", 200),
  MASTER("Master", 500),
}

@Composable
fun StatisticsScreen() {
  val graph = rootGraphAs<StatsGraph>()
  var stats by remember { mutableStateOf(StatsViewState()) }

  LaunchedEffect(Unit) {
    val today = LocalDate.now()
    val weekStart = today.minus(6, ChronoUnit.DAYS)
    val monthStart = today.withDayOfMonth(1)

    val todayMs = graph.listeningSessionDao.totalDurationForDate(today.toString())
    val weekMs = graph.listeningSessionDao.totalDurationBetween(weekStart.toString(), today.toString())
    val monthMs = graph.listeningSessionDao.totalDurationBetween(monthStart.toString(), today.toString())
    val allTimeMs = graph.listeningSessionDao.totalDurationAllTime()

    val minFiveMinMs = 5 * 60 * 1000L
    val datesWithListening = graph.listeningSessionDao.datesWithMinDuration(minFiveMinMs)
    val streak = calculateStreak(datesWithListening)

    val allTimeHours = allTimeMs / (1000 * 60 * 60)
    val level = ListeningLevel.entries.lastOrNull { allTimeHours >= it.thresholdHours }
      ?: ListeningLevel.NEWBIE

    val achievements = graph.achievementDao.allAchievements()

    stats = StatsViewState(
      todayMs = todayMs,
      weekMs = weekMs,
      monthMs = monthMs,
      allTimeMs = allTimeMs,
      currentStreak = streak,
      level = level,
      achievementCount = achievements.size,
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(StringsR.string.statistics_title)) },
        navigationIcon = {
          IconButton(onClick = { graph.navigator.goBack() }) {
            Icon(Icons.Default.Close, contentDescription = stringResource(StringsR.string.close))
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
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = stringResource(StringsR.string.statistics_level, stats.level.label),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        StatCard(
          modifier = Modifier.weight(1f),
          label = stringResource(StringsR.string.statistics_today),
          value = formatTime(stats.todayMs),
        )
        StatCard(
          modifier = Modifier.weight(1f),
          label = stringResource(StringsR.string.statistics_week),
          value = formatTime(stats.weekMs),
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        StatCard(
          modifier = Modifier.weight(1f),
          label = stringResource(StringsR.string.statistics_month),
          value = formatTime(stats.monthMs),
        )
        StatCard(
          modifier = Modifier.weight(1f),
          label = stringResource(StringsR.string.statistics_all_time),
          value = formatTime(stats.allTimeMs),
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        StatCard(
          modifier = Modifier.weight(1f),
          label = stringResource(StringsR.string.statistics_streak),
          value = "${stats.currentStreak} ${stringResource(StringsR.string.statistics_days)}",
        )
        StatCard(
          modifier = Modifier.weight(1f),
          label = stringResource(StringsR.string.statistics_achievements),
          value = "${stats.achievementCount}",
        )
      }

      Spacer(Modifier.height(8.dp))

      Text(
        text = stringResource(StringsR.string.statistics_levels_title),
        style = MaterialTheme.typography.titleMedium,
      )
      ListeningLevel.entries.forEach { level ->
        val reached = stats.allTimeMs / (1000 * 60 * 60) >= level.thresholdHours
        Text(
          text = "${if (reached) "\u2713" else "\u2022"} ${level.label} — ${level.thresholdHours}h+",
          style = MaterialTheme.typography.bodyMedium,
          color = if (reached) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
        )
      }
    }
  }
}

@Composable
private fun StatCard(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

private fun calculateStreak(dates: List<String>): Int {
  if (dates.isEmpty()) return 0
  val sortedDates = dates.map { LocalDate.parse(it) }.sortedDescending()
  val today = LocalDate.now()
  if (sortedDates.first() != today && sortedDates.first() != today.minusDays(1)) return 0

  var streak = 1
  for (i in 1 until sortedDates.size) {
    if (sortedDates[i] == sortedDates[i - 1].minusDays(1)) {
      streak++
    } else {
      break
    }
  }
  return streak
}
