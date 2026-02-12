package eu.kanade.tachiyomi.ui.library.novel

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.base.BasePreferences
import eu.kanade.tachiyomi.data.download.novel.NovelDownloadManager
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tachiyomi.core.common.preference.TriState
import tachiyomi.domain.entries.novel.interactor.GetLibraryNovel
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.library.novel.LibraryNovel
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NovelLibraryScreenModel(
    private val getLibraryNovel: GetLibraryNovel = Injekt.get(),
    private val basePreferences: BasePreferences = Injekt.get(),
    private val libraryPreferences: LibraryPreferences = Injekt.get(),
    private val hasDownloadedChapters: (tachiyomi.domain.entries.novel.model.Novel) -> Boolean = {
        NovelDownloadManager().hasAnyDownloadedChapter(it)
    },
) : StateScreenModel<NovelLibraryScreenModel.State>(
    State(
        downloadedOnly = basePreferences.downloadedOnly().get(),
        downloadedFilter = libraryPreferences.filterDownloadedNovel().get(),
        unreadFilter = libraryPreferences.filterUnreadNovel().get(),
        startedFilter = libraryPreferences.filterStartedNovel().get(),
        bookmarkedFilter = libraryPreferences.filterBookmarkedNovel().get(),
        completedFilter = libraryPreferences.filterCompletedNovel().get(),
    ),
) {

    init {
        screenModelScope.launch {
            getLibraryNovel.subscribe()
                .collectLatest { novels ->
                    mutableState.update { current ->
                        val downloadedNovelIds = if (current.effectiveDownloadedFilter != TriState.DISABLED) {
                            resolveDownloadedNovelIds(novels)
                        } else {
                            emptySet()
                        }
                        current.copy(
                            isLoading = false,
                            rawItems = novels,
                            downloadedNovelIds = downloadedNovelIds,
                            items = filterItems(
                                novels = novels,
                                query = current.searchQuery,
                                downloadedFilter = current.effectiveDownloadedFilter,
                                downloadedNovelIds = downloadedNovelIds,
                                unreadFilter = current.unreadFilter,
                                startedFilter = current.startedFilter,
                                bookmarkedFilter = current.bookmarkedFilter,
                                completedFilter = current.completedFilter,
                            ),
                        )
                    }
                }
        }

        screenModelScope.launch {
            combine(
                basePreferences.downloadedOnly().changes(),
                libraryPreferences.filterDownloadedNovel().changes(),
                libraryPreferences.filterUnreadNovel().changes(),
                libraryPreferences.filterStartedNovel().changes(),
                libraryPreferences.filterBookmarkedNovel().changes(),
            ) { downloadedOnly, downloadedFilter, unreadFilter, startedFilter, bookmarkedFilter ->
                FilterPreferences(
                    downloadedOnly = downloadedOnly,
                    downloadedFilter = downloadedFilter,
                    unreadFilter = unreadFilter,
                    startedFilter = startedFilter,
                    bookmarkedFilter = bookmarkedFilter,
                    completedFilter = TriState.DISABLED,
                )
            }
                .combine(libraryPreferences.filterCompletedNovel().changes()) { prefs, completedFilter ->
                    prefs.copy(completedFilter = completedFilter)
                }
                .collectLatest { prefs ->
                    mutableState.update { current ->
                        val effectiveDownloadedFilter = if (prefs.downloadedOnly) {
                            TriState.ENABLED_IS
                        } else {
                            prefs.downloadedFilter
                        }
                        val downloadedNovelIds = if (effectiveDownloadedFilter != TriState.DISABLED) {
                            resolveDownloadedNovelIds(current.rawItems)
                        } else {
                            emptySet()
                        }
                        current.copy(
                            downloadedOnly = prefs.downloadedOnly,
                            downloadedFilter = prefs.downloadedFilter,
                            unreadFilter = prefs.unreadFilter,
                            startedFilter = prefs.startedFilter,
                            bookmarkedFilter = prefs.bookmarkedFilter,
                            completedFilter = prefs.completedFilter,
                            downloadedNovelIds = downloadedNovelIds,
                            items = filterItems(
                                novels = current.rawItems,
                                query = current.searchQuery,
                                downloadedFilter = effectiveDownloadedFilter,
                                downloadedNovelIds = downloadedNovelIds,
                                unreadFilter = prefs.unreadFilter,
                                startedFilter = prefs.startedFilter,
                                bookmarkedFilter = prefs.bookmarkedFilter,
                                completedFilter = prefs.completedFilter,
                            ),
                        )
                    }
                }
        }
    }

    fun search(query: String?) {
        mutableState.update { current ->
            val trimmed = query?.trim().orEmpty().ifBlank { null }
            current.copy(
                searchQuery = trimmed,
                items = filterItems(
                    novels = current.rawItems,
                    query = trimmed,
                    downloadedFilter = current.effectiveDownloadedFilter,
                    downloadedNovelIds = current.downloadedNovelIds,
                    unreadFilter = current.unreadFilter,
                    startedFilter = current.startedFilter,
                    bookmarkedFilter = current.bookmarkedFilter,
                    completedFilter = current.completedFilter,
                ),
            )
        }
    }

    fun showSettingsDialog() {
        mutableState.update { it.copy(dialog = Dialog.Settings) }
    }

    fun closeDialog() {
        mutableState.update { it.copy(dialog = null) }
    }

    fun toggleDownloadedFilter() {
        setDownloadedFilter(state.value.downloadedFilter.next())
    }

    fun setDownloadedFilter(filter: TriState) {
        libraryPreferences.filterDownloadedNovel().set(filter)
    }

    fun toggleUnreadFilter() {
        setUnreadFilter(state.value.unreadFilter.next())
    }

    fun setUnreadFilter(filter: TriState) {
        libraryPreferences.filterUnreadNovel().set(filter)
    }

    fun toggleStartedFilter() {
        setStartedFilter(state.value.startedFilter.next())
    }

    fun setStartedFilter(filter: TriState) {
        libraryPreferences.filterStartedNovel().set(filter)
    }

    fun toggleBookmarkedFilter() {
        setBookmarkedFilter(state.value.bookmarkedFilter.next())
    }

    fun setBookmarkedFilter(filter: TriState) {
        libraryPreferences.filterBookmarkedNovel().set(filter)
    }

    fun toggleCompletedFilter() {
        setCompletedFilter(state.value.completedFilter.next())
    }

    fun setCompletedFilter(filter: TriState) {
        libraryPreferences.filterCompletedNovel().set(filter)
    }

    private fun filterItems(
        novels: List<LibraryNovel>,
        query: String?,
        downloadedFilter: TriState,
        downloadedNovelIds: Set<Long>,
        unreadFilter: TriState,
        startedFilter: TriState,
        bookmarkedFilter: TriState,
        completedFilter: TriState,
    ): List<LibraryNovel> {
        var filtered = novels
        if (!query.isNullOrBlank()) {
            filtered = filtered.filter { it.novel.title.contains(query, ignoreCase = true) }
        }
        filtered = applyFilter(filtered, downloadedFilter) { it.novel.id in downloadedNovelIds }
        filtered = applyFilter(filtered, unreadFilter) { it.unreadCount > 0 }
        filtered = applyFilter(filtered, startedFilter) { it.hasStarted }
        filtered = applyFilter(filtered, bookmarkedFilter) { it.hasBookmarks }
        filtered = applyFilter(filtered, completedFilter) {
            it.novel.status.toInt() == SManga.COMPLETED
        }
        return filtered
    }

    private fun applyFilter(
        items: List<LibraryNovel>,
        filter: TriState,
        predicate: (LibraryNovel) -> Boolean,
    ): List<LibraryNovel> {
        return when (filter) {
            TriState.DISABLED -> items
            TriState.ENABLED_IS -> items.filter(predicate)
            TriState.ENABLED_NOT -> items.filterNot(predicate)
        }
    }

    private fun resolveDownloadedNovelIds(novels: List<LibraryNovel>): Set<Long> {
        return novels.asSequence()
            .mapNotNull { libraryNovel ->
                libraryNovel.novel.id.takeIf { hasDownloadedChapters(libraryNovel.novel) }
            }
            .toSet()
    }

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val rawItems: List<LibraryNovel> = emptyList(),
        val items: List<LibraryNovel> = emptyList(),
        val searchQuery: String? = null,
        val downloadedOnly: Boolean = false,
        val downloadedFilter: TriState = TriState.DISABLED,
        val unreadFilter: TriState = TriState.DISABLED,
        val startedFilter: TriState = TriState.DISABLED,
        val bookmarkedFilter: TriState = TriState.DISABLED,
        val completedFilter: TriState = TriState.DISABLED,
        val downloadedNovelIds: Set<Long> = emptySet(),
        val dialog: Dialog? = null,
    ) {
        val effectiveDownloadedFilter: TriState
            get() = if (downloadedOnly) TriState.ENABLED_IS else downloadedFilter

        val isLibraryEmpty: Boolean
            get() = rawItems.isEmpty()

        val hasActiveFilters: Boolean
            get() = effectiveDownloadedFilter != TriState.DISABLED ||
                unreadFilter != TriState.DISABLED ||
                startedFilter != TriState.DISABLED ||
                bookmarkedFilter != TriState.DISABLED ||
                completedFilter != TriState.DISABLED
    }

    private data class FilterPreferences(
        val downloadedOnly: Boolean,
        val downloadedFilter: TriState,
        val unreadFilter: TriState,
        val startedFilter: TriState,
        val bookmarkedFilter: TriState,
        val completedFilter: TriState,
    )

    sealed interface Dialog {
        data object Settings : Dialog
    }
}
