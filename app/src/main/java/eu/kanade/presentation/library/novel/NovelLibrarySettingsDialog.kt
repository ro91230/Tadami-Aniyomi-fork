package eu.kanade.presentation.library.novel

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import eu.kanade.presentation.components.TabbedDialog
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.tachiyomi.ui.library.novel.NovelLibraryScreenModel
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.components.SettingsChipRow
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.components.TriStateItem
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun NovelLibrarySettingsDialog(
    onDismissRequest: () -> Unit,
    screenModel: NovelLibraryScreenModel,
) {
    val libraryPreferences = remember { Injekt.get<LibraryPreferences>() }

    TabbedDialog(
        onDismissRequest = onDismissRequest,
        tabTitles = persistentListOf(
            stringResource(MR.strings.action_filter),
            stringResource(MR.strings.action_display),
        ),
    ) { page ->
        Column(
            modifier = Modifier
                .padding(vertical = TabbedDialogPaddings.Vertical)
                .verticalScroll(rememberScrollState()),
        ) {
            when (page) {
                0 -> FilterPage(screenModel)
                1 -> DisplayPage(libraryPreferences)
            }
        }
    }
}

@Composable
private fun ColumnScope.FilterPage(
    screenModel: NovelLibraryScreenModel,
) {
    val state by screenModel.state.collectAsState()

    TriStateItem(
        label = stringResource(MR.strings.label_downloaded),
        state = state.effectiveDownloadedFilter,
        enabled = !state.downloadedOnly,
        onClick = screenModel::setDownloadedFilter,
    )
    TriStateItem(
        label = stringResource(MR.strings.action_filter_unread),
        state = state.unreadFilter,
        onClick = screenModel::setUnreadFilter,
    )
    TriStateItem(
        label = stringResource(MR.strings.label_started),
        state = state.startedFilter,
        onClick = screenModel::setStartedFilter,
    )
    TriStateItem(
        label = stringResource(MR.strings.action_filter_bookmarked),
        state = state.bookmarkedFilter,
        onClick = screenModel::setBookmarkedFilter,
    )
    TriStateItem(
        label = stringResource(MR.strings.completed),
        state = state.completedFilter,
        onClick = screenModel::setCompletedFilter,
    )
}

private val displayModes = listOf(
    MR.strings.action_display_grid to LibraryDisplayMode.CompactGrid,
    MR.strings.action_display_comfortable_grid to LibraryDisplayMode.ComfortableGrid,
    MR.strings.action_display_cover_only_grid to LibraryDisplayMode.CoverOnlyGrid,
    MR.strings.action_display_list to LibraryDisplayMode.List,
)

@Composable
private fun ColumnScope.DisplayPage(
    libraryPreferences: LibraryPreferences,
) {
    val displayMode by libraryPreferences.displayMode().collectAsState()
    SettingsChipRow(MR.strings.action_display_mode) {
        displayModes.map { (titleRes, mode) ->
            FilterChip(
                selected = displayMode == mode,
                onClick = { libraryPreferences.displayMode().set(mode) },
                label = { Text(stringResource(titleRes)) },
            )
        }
    }

    val configuration = LocalConfiguration.current
    val columnPreference = remember(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            libraryPreferences.novelLandscapeColumns()
        } else {
            libraryPreferences.novelPortraitColumns()
        }
    }

    val columns by columnPreference.collectAsState()
    if (displayMode == LibraryDisplayMode.List) {
        SliderItem(
            value = columns,
            valueRange = 0..10,
            label = stringResource(AYMR.strings.pref_library_rows),
            valueText = if (columns > 0) {
                columns.toString()
            } else {
                stringResource(MR.strings.label_auto)
            },
            onChange = columnPreference::set,
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    } else {
        SliderItem(
            value = columns,
            valueRange = 0..10,
            label = stringResource(MR.strings.pref_library_columns),
            valueText = if (columns > 0) {
                columns.toString()
            } else {
                stringResource(MR.strings.label_auto)
            },
            onChange = columnPreference::set,
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }
}
