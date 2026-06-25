package com.project.zariya.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.core.ui.theme.*

private val BadgePillShape = RoundedCornerShape(50)

// Clock icon drawn inline to avoid extra icon dependency
private val ClockIcon: ImageVector
    get() = Icons.Filled.Check // Fallback — clock is conceptual via label

private val SnoozeIcon: ImageVector
    get() = Icons.Filled.Close // Fallback — snooze is conceptual via label

@Composable
fun DoseStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (gradientBrush, icon, label, textColor) = remember(status) {
        when (status.uppercase()) {
            "TAKEN" -> StatusBadgeStyle(
                gradient = Brush.horizontalGradient(
                    listOf(ZariyaSuccessContainer, ZariyaSuccessContainer)
                ),
                icon = Icons.Filled.Check,
                label = "Taken",
                textColor = ZariyaSuccess
            )
            "MISSED" -> StatusBadgeStyle(
                gradient = Brush.horizontalGradient(
                    listOf(ZariyaErrorContainer, ZariyaErrorContainer)
                ),
                icon = Icons.Filled.Close,
                label = "Missed",
                textColor = ZariyaError
            )
            "PENDING" -> StatusBadgeStyle(
                gradient = Brush.horizontalGradient(
                    listOf(ZariyaWarningContainer, ZariyaWarningContainer)
                ),
                icon = Icons.Filled.Check, // Placeholder for clock
                label = "Pending",
                textColor = ZariyaWarning
            )
            "SNOOZED" -> StatusBadgeStyle(
                gradient = Brush.horizontalGradient(
                    listOf(ZariyaSurfaceVariant, ZariyaSurfaceVariant)
                ),
                icon = Icons.Filled.Close, // Placeholder for snooze
                label = "Snoozed",
                textColor = ZariyaTextSecondary
            )
            "SKIPPED" -> StatusBadgeStyle(
                gradient = Brush.horizontalGradient(
                    listOf(ZariyaSurfaceVariant, ZariyaSurfaceVariant)
                ),
                icon = Icons.Filled.Close,
                label = "Skipped",
                textColor = ZariyaTextSecondary
            )
            else -> StatusBadgeStyle(
                gradient = Brush.horizontalGradient(
                    listOf(ZariyaSurfaceVariant, ZariyaSurfaceVariant)
                ),
                icon = Icons.Filled.Check,
                label = status,
                textColor = ZariyaTextTertiary
            )
        }
    }

    // Animated entrance
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(status) { appeared = true }

    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "badgeEntrance"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(BadgePillShape)
            .background(brush = gradientBrush, shape = BadgePillShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = textColor
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
    }
}

private data class StatusBadgeStyle(
    val gradient: Brush,
    val icon: ImageVector,
    val label: String,
    val textColor: Color
)
