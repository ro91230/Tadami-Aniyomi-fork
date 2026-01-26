package eu.kanade.presentation.achievement.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.achievement.screenmodel.AchievementScreenState
import tachiyomi.presentation.core.components.material.padding

@Composable
fun AchievementContent(
    state: AchievementScreenState,
    modifier: Modifier = Modifier,
    onAchievementClick: (achievement: tachiyomi.domain.achievement.model.Achievement) -> Unit = {},
    onDialogDismiss: () -> Unit = {},
) {
    when (state) {
        is AchievementScreenState.Loading -> {
            AchievementLoading(modifier = modifier)
        }
        is AchievementScreenState.Success -> {
            AchievementGrid(
                state = state,
                modifier = modifier,
                onAchievementClick = onAchievementClick,
            )

            // Show detail dialog if achievement is selected
            if (state.selectedAchievement != null) {
                val achievement = state.selectedAchievement!!
                val progress = state.progress[achievement.id]
                AchievementDetailDialog(
                    achievement = achievement,
                    progress = progress,
                    onDismiss = onDialogDismiss,
                )
            }
        }
    }
}

@Composable
private fun AchievementLoading(
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AchievementGrid(
    state: AchievementScreenState.Success,
    modifier: Modifier = Modifier,
    onAchievementClick: (achievement: tachiyomi.domain.achievement.model.Achievement) -> Unit,
) {
    val filteredAchievements = state.filteredAchievements

    if (filteredAchievements.isEmpty()) {
        androidx.compose.foundation.layout.Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Нет достижений в этой категории",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.padding.medium,
            vertical = MaterialTheme.padding.medium,
        ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
    ) {
        items(
            items = filteredAchievements,
            key = { it.id },
        ) { achievement ->
            val progress = state.progress[achievement.id]
            AchievementCard(
                achievement = achievement,
                progress = progress,
                onClick = { onAchievementClick(achievement) },
            )
        }
    }
}
