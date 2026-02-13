package eu.kanade.tachiyomi.ui.entries.novel

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class NovelScreenModelScanlatorSelectionTest {

    @Test
    fun `resolveSelectedScanlator returns null when all scanlators are visible`() {
        resolveSelectedNovelScanlator(
            availableScanlators = setOf("Team A", "Team B"),
            excludedScanlators = emptySet(),
        ) shouldBe null
    }

    @Test
    fun `resolveSelectedScanlator returns only included scanlator`() {
        resolveSelectedNovelScanlator(
            availableScanlators = setOf("Team A", "Team B"),
            excludedScanlators = setOf("Team B"),
        ) shouldBe "Team A"
    }

    @Test
    fun `resolveSelectedScanlator returns null when no scanlators remain`() {
        resolveSelectedNovelScanlator(
            availableScanlators = setOf("Team A"),
            excludedScanlators = setOf("Team A"),
        ) shouldBe null
    }

    @Test
    fun `resolveExcludedScanlatorsForSelection clears exclusions for all branches`() {
        resolveNovelExcludedScanlatorsForSelection(
            selectedScanlator = null,
            availableScanlators = setOf("Team A", "Team B"),
        ) shouldBe emptySet()
    }

    @Test
    fun `resolveExcludedScanlatorsForSelection excludes all except selected branch`() {
        resolveNovelExcludedScanlatorsForSelection(
            selectedScanlator = "Team B",
            availableScanlators = setOf("Team A", "Team B", "Team C"),
        ) shouldBe setOf("Team A", "Team C")
    }

    @Test
    fun `resolveExcludedScanlatorsForSelection handles extra spaces in available values`() {
        resolveNovelExcludedScanlatorsForSelection(
            selectedScanlator = "Team B",
            availableScanlators = setOf(" Team A ", "Team B ", "Team C"),
        ) shouldBe setOf("Team A", "Team C")
    }
}
