package tachiyomi.data.achievement.handler

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tachiyomi.data.achievement.database.AchievementsDatabase
import tachiyomi.domain.achievement.model.UserPoints
import kotlin.math.sqrt

class PointsManager(
    private val database: AchievementsDatabase,
) {

    init {
        // Initialize stats on creation
        initializeStats()
    }

    private fun initializeStats() {
        database.achievementProgressQueries.initUserStats()
    }

    suspend fun addPoints(points: Int) {
        if (points > 0) {
            withContext(Dispatchers.IO) {
                database.achievementProgressQueries.addPoints(points.toLong())
                recalculateLevel()
            }
        }
    }

    suspend fun incrementUnlocked() {
        withContext(Dispatchers.IO) {
            database.achievementProgressQueries.incrementUnlocked()
        }
    }

    fun subscribeToPoints(): Flow<UserPoints> {
        return database.achievementProgressQueries.getUserStats()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map {
                it?.let { stats ->
                    UserPoints(
                        totalPoints = stats.total_points.toInt(),
                        level = stats.level.toInt(),
                        achievementsUnlocked = stats.achievements_unlocked.toInt(),
                    )
                } ?: UserPoints()
            }
    }

    suspend fun getCurrentPoints(): UserPoints {
        return withContext(Dispatchers.IO) {
            try {
                database.achievementProgressQueries.getUserStats()
                    .executeAsOne()
                    .let {
                        UserPoints(
                            totalPoints = it.total_points.toInt(),
                            level = it.level.toInt(),
                            achievementsUnlocked = it.achievements_unlocked.toInt(),
                        )
                    }
            } catch (e: Exception) {
                initializeStats()
                UserPoints()
            }
        }
    }

    private suspend fun recalculateLevel() {
        val current = getCurrentPoints()
        val newLevel = calculateLevel(current.totalPoints)
        database.achievementProgressQueries.setLevel(newLevel.toLong())
    }

    fun calculateLevel(points: Int): Int {
        // Formula: level = sqrt(points / 100) + 1
        return (sqrt(points.toDouble() / 100.0)).toInt() + 1
    }
}
