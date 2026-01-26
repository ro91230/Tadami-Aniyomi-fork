package eu.kanade.presentation.achievement.screenmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tachiyomi.domain.achievement.model.Achievement
import tachiyomi.domain.achievement.model.AchievementCategory
import tachiyomi.domain.achievement.model.AchievementProgress
import tachiyomi.domain.achievement.repository.AchievementRepository
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AchievementScreenModel(
    private val repository: AchievementRepository = Injekt.get(),
) : StateScreenModel<AchievementScreenState>(AchievementScreenState.Loading) {

    init {
        screenModelScope.launch {
            combine(
                repository.getAll(),
                repository.getAllProgress(),
            ) { achievements, progress ->
                AchievementScreenState.Success(
                    achievements = achievements,
                    progress = progress.associateBy { it.achievementId },
                    selectedCategory = AchievementCategory.BOTH,
                )
            }.collect { state ->
                mutableState.update { state }
            }
        }
    }

    fun onCategoryChanged(category: AchievementCategory) {
        mutableState.update {
            when (it) {
                AchievementScreenState.Loading -> it
                is AchievementScreenState.Success -> it.copy(selectedCategory = category)
            }
        }
    }

    fun onAchievementClick(achievement: Achievement) {
        mutableState.update {
            when (it) {
                AchievementScreenState.Loading -> it
                is AchievementScreenState.Success -> it.copy(selectedAchievement = achievement)
            }
        }
    }

    fun onDialogDismiss() {
        mutableState.update {
            when (it) {
                AchievementScreenState.Loading -> it
                is AchievementScreenState.Success -> it.copy(selectedAchievement = null)
            }
        }
    }
}

@Immutable
sealed interface AchievementScreenState {
    @Immutable
    data object Loading : AchievementScreenState

    @Immutable
    data class Success(
        val achievements: List<Achievement> = emptyList(),
        val progress: Map<String, AchievementProgress> = emptyMap(),
        val selectedCategory: AchievementCategory = AchievementCategory.BOTH,
        val selectedAchievement: Achievement? = null,
    ) : AchievementScreenState {
        val filteredAchievements: List<Achievement>
            get() = when (selectedCategory) {
                AchievementCategory.BOTH -> achievements.filter { it.category == AchievementCategory.BOTH }
                AchievementCategory.ANIME -> achievements.filter { it.category == AchievementCategory.ANIME }
                AchievementCategory.MANGA -> achievements.filter { it.category == AchievementCategory.MANGA }
                AchievementCategory.SECRET -> achievements.filter { it.category == AchievementCategory.SECRET }
            }
    }
}
