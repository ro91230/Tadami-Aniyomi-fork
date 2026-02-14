package eu.kanade.tachiyomi.ui.stats.novel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.TabContent
import eu.kanade.presentation.more.stats.NovelStatsScreenContent
import eu.kanade.presentation.more.stats.StatsScreenState
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.screens.LoadingScreen

@Composable
fun Screen.novelStatsTab(): TabContent {
    val navigator = LocalNavigator.currentOrThrow

    val screenModel = rememberScreenModel { NovelStatsScreenModel() }
    val state by screenModel.state.collectAsState()

    if (state is StatsScreenState.Loading) {
        LoadingScreen()
    }

    return TabContent(
        titleRes = AYMR.strings.label_novel,
        content = { contentPadding, _ ->
            if (state is StatsScreenState.Loading) {
                LoadingScreen()
            } else {
                NovelStatsScreenContent(
                    state = state as StatsScreenState.SuccessNovel,
                    paddingValues = contentPadding,
                )
            }
        },
        navigateUp = navigator::pop,
    )
}
