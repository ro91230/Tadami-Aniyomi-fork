package eu.kanade.presentation.achievement.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.theme.AuroraTheme
import tachiyomi.domain.achievement.model.Achievement
import tachiyomi.domain.achievement.model.AchievementProgress

@Composable
fun AchievementCard(
    achievement: Achievement,
    progress: AchievementProgress?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AuroraTheme.colors
    val isUnlocked = progress?.isUnlocked == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                colors.glass.copy(alpha = 0.8f)
            } else {
                colors.glass.copy(alpha = 0.4f)
            },
        ),
        border = CardDefaults.cardBorder(
            border = if (colors.isDark) {
                androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isUnlocked) {
                        colors.primary.copy(alpha = 0.3f)
                    } else {
                        Color.Transparent
                    },
                )
            } else {
                null
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Header row: Icon, Title, Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Achievement Icon
                if (achievement.isHidden && !isUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    AchievementIcon(
                        achievement = achievement,
                        isUnlocked = isUnlocked,
                        modifier = Modifier.size(48.dp),
                        size = 48.dp,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Points
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = if (achievement.isHidden && !isUnlocked) {
                            "???"
                        } else {
                            achievement.title
                        },
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (achievement.points > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${achievement.points} очков",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }

                // Status Badge
                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Получено",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Description
            if (!achievement.isHidden || isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                achievement.description?.let { description ->
                    Text(
                        text = description,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                    )
                }
            }

            // Progress Bar (if not unlocked and has threshold)
            if (!isUnlocked && achievement.threshold != null && progress != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Прогресс",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "${progress.progress}/${achievement.threshold}",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress.progress.toFloat() / achievement.threshold.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = colors.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}
