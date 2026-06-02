package voice.features.stats

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.LocalDate

class CalculateStreakTest {

  private val today = LocalDate.now()

  @Test
  fun `empty list returns 0`() {
    calculateStreak(emptyList()) shouldBe 0
  }

  @Test
  fun `single day today returns streak of 1`() {
    calculateStreak(listOf(today.toString())) shouldBe 1
  }

  @Test
  fun `single day yesterday returns streak of 1`() {
    calculateStreak(listOf(today.minusDays(1).toString())) shouldBe 1
  }

  @Test
  fun `two days ago with no today or yesterday returns 0`() {
    calculateStreak(listOf(today.minusDays(2).toString())) shouldBe 0
  }

  @Test
  fun `consecutive days from today`() {
    val dates = (0L..4L).map { today.minusDays(it).toString() }
    calculateStreak(dates) shouldBe 5
  }

  @Test
  fun `gap breaks streak`() {
    val dates = listOf(
      today.toString(),
      today.minusDays(1).toString(),
      today.minusDays(3).toString(),
    )
    calculateStreak(dates) shouldBe 2
  }

  @Test
  fun `unordered dates are sorted correctly`() {
    val dates = listOf(
      today.minusDays(2).toString(),
      today.toString(),
      today.minusDays(1).toString(),
    )
    calculateStreak(dates) shouldBe 3
  }

  @Test
  fun `streak starting from yesterday`() {
    val dates = listOf(
      today.minusDays(1).toString(),
      today.minusDays(2).toString(),
      today.minusDays(3).toString(),
    )
    calculateStreak(dates) shouldBe 3
  }
}
