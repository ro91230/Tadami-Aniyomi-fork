package eu.kanade.tachiyomi.ui.library.novel

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import eu.kanade.tachiyomi.source.model.SManga
import tachiyomi.domain.entries.novel.interactor.GetLibraryNovel
import tachiyomi.domain.entries.novel.model.Novel
import tachiyomi.domain.library.novel.LibraryNovel

class NovelLibraryScreenModelTest {

    private val getLibraryNovel: GetLibraryNovel = mockk()
    private val libraryFlow = MutableStateFlow<List<LibraryNovel>>(emptyList())
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getLibraryNovel.subscribe() } returns libraryFlow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filters library novels by search query`() = runTest {
        val first = libraryNovel(id = 1L, title = "First Novel")
        val second = libraryNovel(id = 2L, title = "Second Story")
        libraryFlow.value = listOf(first, second)

        val screenModel = NovelLibraryScreenModel(getLibraryNovel = getLibraryNovel)

        testDispatcher.scheduler.advanceUntilIdle()
        screenModel.search("Second")
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.state.value.items.shouldContainExactly(second)
    }

    @Test
    fun `unread filter keeps only unread entries`() = runTest {
        val unread = libraryNovel(id = 1L, title = "Unread", total = 10L, read = 1L)
        val read = libraryNovel(id = 2L, title = "Read", total = 10L, read = 10L)
        libraryFlow.value = listOf(unread, read)

        val screenModel = NovelLibraryScreenModel(getLibraryNovel = getLibraryNovel)

        testDispatcher.scheduler.advanceUntilIdle()
        screenModel.toggleUnreadFilter()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.state.value.items.shouldContainExactly(unread)
        screenModel.state.value.hasActiveFilters shouldBe true
    }

    @Test
    fun `completed filter keeps only completed entries`() = runTest {
        val ongoing = libraryNovel(id = 1L, title = "Ongoing", status = SManga.ONGOING.toLong())
        val completed = libraryNovel(id = 2L, title = "Completed", status = SManga.COMPLETED.toLong())
        libraryFlow.value = listOf(ongoing, completed)

        val screenModel = NovelLibraryScreenModel(getLibraryNovel = getLibraryNovel)

        testDispatcher.scheduler.advanceUntilIdle()
        screenModel.toggleCompletedFilter()
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.state.value.items.shouldContainExactly(completed)
    }

    private fun libraryNovel(
        id: Long,
        title: String,
        total: Long = 10L,
        read: Long = 1L,
        status: Long = 0L,
    ): LibraryNovel {
        return LibraryNovel(
            novel = Novel.create().copy(
                id = id,
                title = title,
                url = "https://example.com/$id",
                source = 1L,
                favorite = true,
                status = status,
            ),
            category = 0L,
            totalChapters = total,
            readCount = read,
            bookmarkCount = 0L,
            latestUpload = 0L,
            chapterFetchedAt = 0L,
            lastRead = 0L,
        )
    }
}
