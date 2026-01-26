package eu.kanade.presentation.achievement.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.R
import tachiyomi.domain.achievement.model.Achievement

/**
 * Composable icon for achievement badges.
 *
 * @param icon The icon name from achievement.badge_icon
 * @param isUnlocked Whether the achievement is unlocked
 * @param modifier The modifier to be applied to the icon
 * @param size The size of the icon
 * @param useCircleShape Whether to use circle shape (default) or rounded rectangle
 */
@Composable
fun AchievementIcon(
    icon: String?,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    useCircleShape: Boolean = false,
) {
    val context = LocalContext.current
    val iconResId = getIconResourceId(icon, context.packageName)

    val backgroundColor = if (isUnlocked) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Gray.copy(alpha = 0.1f)
    }

    val shape = if (useCircleShape) CircleShape else RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundColor),
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .alpha(if (isUnlocked) 1f else 0.5f),
            contentScale = ContentScale.Fit,
            colorFilter = if (!isUnlocked) {
                // Apply grayscale filter for locked achievements
                ColorFilter.colorMatrix(
                    ColorMatrix().apply {
                        setToSaturation(0f)
                    },
                )
            } else {
                null
            },
        )
    }
}

/**
 * Resolves the icon resource ID from the icon name.
 *
 * @param iconName The name of the icon resource (e.g., "ic_badge_first_chapter")
 * @param packageName The package name of the app
 * @return The resource ID, defaults to ic_badge_default if not found
 */
private fun getIconResourceId(
    iconName: String?,
    packageName: String,
): Int {
    if (iconName.isNullOrEmpty()) {
        return R.drawable.ic_badge_default
    }

    return try {
        val resourceId = android.content.res.Resources.getSystem().getIdentifier(
            iconName,
            "drawable",
            packageName,
        )
        if (resourceId != 0) {
            resourceId
        } else {
            R.drawable.ic_badge_default
        }
    } catch (e: Exception) {
        R.drawable.ic_badge_default
    }
}

/**
 * Simplified version that takes an Achievement directly.
 */
@Composable
fun AchievementIcon(
    achievement: Achievement,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    useCircleShape: Boolean = false,
) {
    AchievementIcon(
        icon = achievement.badgeIcon,
        isUnlocked = isUnlocked,
        modifier = modifier,
        size = size,
        useCircleShape = useCircleShape,
    )
}
