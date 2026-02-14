package eu.kanade.presentation.library.novel

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.library.manga.model.MangaLibrarySort
import tachiyomi.domain.library.model.LibraryDisplayMode

class NovelLibrarySettingsDialogOptionsTest {

    @Test
    fun `novel sort options contain expected parity modes`() {
        novelLibrarySortOptions().map { it.second } shouldBe listOf(
            MangaLibrarySort.Type.Alphabetical,
            MangaLibrarySort.Type.TotalChapters,
            MangaLibrarySort.Type.LastRead,
            MangaLibrarySort.Type.LastUpdate,
            MangaLibrarySort.Type.UnreadCount,
            MangaLibrarySort.Type.LatestChapter,
            MangaLibrarySort.Type.ChapterFetchDate,
            MangaLibrarySort.Type.DateAdded,
            MangaLibrarySort.Type.Random,
        )
    }

    @Test
    fun `novel display modes contain expected parity modes`() {
        novelLibraryDisplayModes().map { it.second } shouldBe listOf(
            LibraryDisplayMode.CompactGrid,
            LibraryDisplayMode.ComfortableGrid,
            LibraryDisplayMode.CoverOnlyGrid,
            LibraryDisplayMode.List,
        )
    }
}
