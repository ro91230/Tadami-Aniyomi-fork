package eu.kanade.presentation.achievement.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.theme.AuroraTheme
import tachiyomi.data.achievement.UnlockableManager
import tachiyomi.domain.achievement.model.Achievement
import tachiyomi.domain.achievement.model.AchievementProgress
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AchievementDetailDialog(
    achievement: Achievement,
    progress: AchievementProgress?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    unlockableManager: UnlockableManager = Injekt.get(),
) {
    val colors = AuroraTheme.colors
    val isUnlocked = progress?.isUnlocked == true

    AdaptiveSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.glass.copy(alpha = 0.95f),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Icon (large)
                if (achievement.isHidden && !isUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                } else {
                    AchievementIcon(
                        achievement = achievement,
                        isUnlocked = isUnlocked,
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally),
                        size = 80.dp,
                        useCircleShape = true,
                    )
                }

                // Title
                Text(
                    text = if (achievement.isHidden && !isUnlocked) {
                        "???"
                    } else {
                        achievement.title
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = colors.textPrimary,
                )

                // Description
                if (!achievement.isHidden || isUnlocked) {
                    if (!achievement.description.isNullOrBlank()) {
                        Text(
                            text = achievement.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = colors.textSecondary,
                        )
                    }
                }

                // Progress
                if (progress != null && !progress.isUnlocked) {
                    ProgressSection(progress, achievement.threshold, colors)
                }

                // Divider
                HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.2f))

                // Rewards
                RewardSection(achievement, isUnlocked, unlockableManager, colors)

                // Unlock date
                if (progress?.unlockedAt != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Разблокировано: ${formatDate(progress.unlockedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

@Composable
private fun ProgressSection(
    progress: AchievementProgress,
    threshold: Int?,
    colors: eu.kanade.presentation.theme.AuroraColors,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val max = threshold ?: progress.maxProgress
        val progressFraction = (progress.progress.toFloat() / max).coerceIn(0f, 1f)

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = colors.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$progress.progress / $max",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Text(
                text = "${(progressFraction * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun RewardSection(
    achievement: Achievement,
    isUnlocked: Boolean,
    unlockableManager: UnlockableManager,
    colors: eu.kanade.presentation.theme.AuroraColors,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Награды:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "+${achievement.points} очков",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        if (achievement.unlockableId != null) {
            val isUnlockableUnlocked = unlockableManager.isUnlockableUnlocked(achievement.unlockableId)
            val unlockableName = unlockableManager.getUnlockableName(achievement.unlockableId)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (isUnlockableUnlocked) Icons.Default.Check else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlockableUnlocked) Color(0xFF4CAF50) else colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Column {
                    Text(
                        text = unlockableName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUnlockableUnlocked) colors.textPrimary else colors.textSecondary,
                        fontWeight = if (isUnlockableUnlocked) FontWeight.Bold else FontWeight.Normal,
                    )
                    if (!isUnlockableUnlocked) {
                        Text(
                            text = "Разблокируется при выполнении достижения",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("ru"))
    return formatter.format(Date(timestamp))
}
