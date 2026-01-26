package eu.kanade.presentation.achievement.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.achievement.screenmodel.AchievementScreenState
import tachiyomi.domain.achievement.model.Achievement
import tachiyomi.domain.achievement.model.AchievementCategory
import tachiyomi.presentation.core.components.material.padding

@Composable
fun AchievementTabsAndGrid(
    state: AchievementScreenState.Success,
    onAchievementClick: (achievement: Achievement) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf(state.selectedCategory) }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        // Category Tabs
        AchievementTabs(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
            },
        )

        // Content
        AchievementContent(
            state = state.copy(selectedCategory = selectedCategory, selectedAchievement = state.selectedAchievement),
            onAchievementClick = onAchievementClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun AchievementTabs(
    selectedCategory: AchievementCategory,
    onCategorySelected: (AchievementCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf(
        AchievementCategory.BOTH to "Все",
        AchievementCategory.ANIME to "Аниме",
        AchievementCategory.MANGA to "Манга",
        AchievementCategory.SECRET to "Секретные",
    )

    TabRow(
        selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    tabPositions[categories.indexOfFirst { it.first == selectedCategory}],
                ),
                height = 2.dp,
            )
        },
        divider = {},
        modifier = modifier,
    ) {
        categories.forEach { (category, title) ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = title,
                        style = if (selectedCategory == category) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                    )
                },
            )
        }
    }
}
