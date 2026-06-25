package com.project.zariya.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.zariya.core.ui.icons.ZariyaIcons
import com.project.zariya.core.ui.theme.*

@Composable
fun MedicineAvatar(
    form: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val (icon, bgColor) = when (form.uppercase()) {
        "TABLET" -> ZariyaIcons.Capsule to TabletColor
        "CAPSULE" -> ZariyaIcons.Capsule to CapsuleColor
        "SYRUP" -> ZariyaIcons.PillBottle to SyrupColor
        "INJECTION" -> ZariyaIcons.PillBottle to InjectionColor
        "DROPS" -> ZariyaIcons.PillBottle to DropsColor
        "INHALER" -> ZariyaIcons.PillBottle to InhalerColor
        "CREAM" -> ZariyaIcons.PillBottle to CreamColor
        "PATCH" -> ZariyaIcons.PillBottle to PatchColor
        else -> ZariyaIcons.PillBottle to ZariyaPrimary
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = form,
            tint = bgColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
