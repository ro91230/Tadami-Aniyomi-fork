package eu.kanade.presentation.achievement.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.presentation.achievement.components.AchievementContent
import eu.kanade.presentation.achievement.components.AchievementTabsAndGrid
import eu.kanade.presentation.achievement.screenmodel.AchievementScreenState
import eu.kanade.presentation.components.AppBar
import tachiyomi.domain.achievement.model.Achievement
import tachiyomi.presentation.core.components.material.Scaffold

@Composable
fun AchievementScreen(
    state: AchievementScreenState,
    onClickBack: () -> Unit,
    onAchievementClick: (achievement: Achievement) -> Unit = {},
    onDialogDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = "Достижения",
                navigateUp = onClickBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        when (state) {
            is AchievementScreenState.Loading -> {
                AchievementContent(
                    state = state,
                    modifier = modifier.fillMaxSize(),
                    onAchievementClick = onAchievementClick,
                    onDialogDismiss = onDialogDismiss,
                )
            }
            is AchievementScreenState.Success -> {
                AchievementTabsAndGrid(
                    state = state,
                    onAchievementClick = onAchievementClick,
                    modifier = modifier,
                )

                // Show detail dialog if achievement is selected
                if (state.selectedAchievement != null) {
                    eu.kanade.presentation.achievement.components.AchievementDetailDialog(
                        achievement = state.selectedAchievement!!,
                        progress = state.progress[state.selectedAchievement!!.id],
                        onDismiss = onDialogDismiss,
                    )
                }
            }
        }
    }
}
