package eu.kanade.presentation.achievement.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tachiyomi.domain.achievement.model.Achievement

@Composable
fun AchievementUnlockBanner(
    modifier: Modifier = Modifier,
) {
    var currentAchievement by remember { mutableStateOf<Achievement?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    // Register callback with manager
    LaunchedEffect(Unit) {
        AchievementBannerManager.setOnShowCallback { achievement ->
            if (!isVisible) {
                currentAchievement = achievement
            }
        }
    }

    // Auto-dismiss after delay
    LaunchedEffect(currentAchievement) {
        if (currentAchievement != null) {
            isVisible = true
            delay(5000) // Show for 5 seconds
            isVisible = false
            delay(300) // Wait for exit animation
            currentAchievement = null
        }
    }

    // Animation for banner appearance
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "banner_alpha",
    )

    AnimatedVisibility(
        visible = currentAchievement != null && isVisible,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) + fadeIn(),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Top,
        ) + fadeOut(),
        modifier = modifier,
    ) {
        val achievement = currentAchievement
        if (achievement != null) {
            AchievementBannerItem(
                achievement = achievement,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .alpha(alpha),
            )
        }
    }
}

@Composable
private fun AchievementBannerItem(
    achievement: Achievement,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFD700), // Gold
                        Color(0xFFFFA500), // Orange
                    ),
                ),
            )
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column {
                Text(
                    text = "Achievement Unlocked!",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = achievement.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )

                achievement.description?.let { description ->
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                    )
                }

                Text(
                    text = "+${achievement.points} points",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// Global manager for showing achievement unlock banners
object AchievementBannerManager {
    private var onShowCallback: ((Achievement) -> Unit)? = null

    fun setOnShowCallback(callback: (Achievement) -> Unit) {
        onShowCallback = callback
    }

    fun showAchievement(achievement: Achievement) {
        onShowCallback?.invoke(achievement)
    }

    fun clear() {
        onShowCallback = null
    }
}
