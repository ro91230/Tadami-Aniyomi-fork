package eu.kanade.tachiyomi.ui.library.novel

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tachiyomi.core.common.preference.TriState
import tachiyomi.domain.entries.novel.interactor.GetLibraryNovel
import tachiyomi.domain.library.novel.LibraryNovel
import eu.kanade.tachiyomi.source.model.SManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NovelLibraryScreenModel(
    private val getLibraryNovel: GetLibraryNovel = Injekt.get(),
) : StateScreenModel<NovelLibraryScreenModel.State>(State()) {

    init {
        screenModelScope.launch {
            getLibraryNovel.subscribe()
                .collectLatest { novels ->
                    mutableState.update { current ->
                        current.copy(
                            isLoading = false,
                            rawItems = novels,
                            items = filterItems(
                                novels = novels,
                                query = current.searchQuery,
                                unreadFilter = current.unreadFilter,
                                startedFilter = current.startedFilter,
                                bookmarkedFilter = current.bookmarkedFilter,
                                completedFilter = current.completedFilter,
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

    fun toggleUnreadFilter() {
        updateFilters(unreadFilter = state.value.unreadFilter.next())
    }

    fun setUnreadFilter(filter: TriState) {
        updateFilters(unreadFilter = filter)
    }

    fun toggleStartedFilter() {
        updateFilters(startedFilter = state.value.startedFilter.next())
    }

    fun setStartedFilter(filter: TriState) {
        updateFilters(startedFilter = filter)
    }

    fun toggleBookmarkedFilter() {
        updateFilters(bookmarkedFilter = state.value.bookmarkedFilter.next())
    }

    fun setBookmarkedFilter(filter: TriState) {
        updateFilters(bookmarkedFilter = filter)
    }

    fun toggleCompletedFilter() {
        updateFilters(completedFilter = state.value.completedFilter.next())
    }

    fun setCompletedFilter(filter: TriState) {
        updateFilters(completedFilter = filter)
    }

    private fun updateFilters(
        unreadFilter: TriState = state.value.unreadFilter,
        startedFilter: TriState = state.value.startedFilter,
        bookmarkedFilter: TriState = state.value.bookmarkedFilter,
        completedFilter: TriState = state.value.completedFilter,
    ) {
        mutableState.update { current ->
            current.copy(
                unreadFilter = unreadFilter,
                startedFilter = startedFilter,
                bookmarkedFilter = bookmarkedFilter,
                completedFilter = completedFilter,
                items = filterItems(
                    novels = current.rawItems,
                    query = current.searchQuery,
                    unreadFilter = unreadFilter,
                    startedFilter = startedFilter,
                    bookmarkedFilter = bookmarkedFilter,
                    completedFilter = completedFilter,
                ),
            )
        }
    }

    private fun filterItems(
        novels: List<LibraryNovel>,
        query: String?,
        unreadFilter: TriState,
        startedFilter: TriState,
        bookmarkedFilter: TriState,
        completedFilter: TriState,
    ): List<LibraryNovel> {
        var filtered = novels
        if (!query.isNullOrBlank()) {
            filtered = filtered.filter { it.novel.title.contains(query, ignoreCase = true) }
        }
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

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val rawItems: List<LibraryNovel> = emptyList(),
        val items: List<LibraryNovel> = emptyList(),
        val searchQuery: String? = null,
        val unreadFilter: TriState = TriState.DISABLED,
        val startedFilter: TriState = TriState.DISABLED,
        val bookmarkedFilter: TriState = TriState.DISABLED,
        val completedFilter: TriState = TriState.DISABLED,
        val dialog: Dialog? = null,
    ) {
        val isLibraryEmpty: Boolean
            get() = rawItems.isEmpty()

        val hasActiveFilters: Boolean
            get() = unreadFilter != TriState.DISABLED ||
                startedFilter != TriState.DISABLED ||
                bookmarkedFilter != TriState.DISABLED ||
                completedFilter != TriState.DISABLED
    }

    sealed interface Dialog {
        data object Settings : Dialog
    }
}
