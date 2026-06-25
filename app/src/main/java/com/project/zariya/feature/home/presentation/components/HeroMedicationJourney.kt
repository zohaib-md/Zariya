package com.project.zariya.feature.home.presentation.components

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
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
import com.project.zariya.core.ui.icons.ZariyaIcons
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import com.project.zariya.core.ui.theme.ZariyaTextTertiary
import com.project.zariya.core.util.DateTimeUtils
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus

@Composable
fun HeroMedicationJourney(
    todayLogs: List<DoseLog>,
    modifier: Modifier = Modifier
) {
    val total = todayLogs.size
    val taken = todayLogs.count { it.status == DoseStatus.TAKEN }
    
    val pending = todayLogs.filter { it.status == DoseStatus.PENDING }
    val nextDose = pending.minByOrNull { it.scheduledTime }
    val completedDoses = todayLogs.filter { it.status == DoseStatus.TAKEN }.sortedByDescending { it.actionTime ?: it.scheduledTime }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ZariyaSurface)
            .padding(20.dp)
    ) {
        // Header
        Text(
            text = "Today's Journey",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ZariyaTextPrimary
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = "Progress: $taken of $total completed",
            fontSize = 14.sp,
            color = ZariyaTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Timeline
        if (todayLogs.isEmpty()) {
            Text(
                text = "No medications scheduled for today.",
                fontSize = 14.sp,
                color = ZariyaTextTertiary
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Next Dose
                if (nextDose != null) {
                    JourneyTimelineItem(
                        icon = Icons.Outlined.Schedule,
                        title = nextDose.medicineName,
                        subtitle = "Scheduled for ${DateTimeUtils.run { nextDose.scheduledTime.formatTime() }}",
                        iconTint = Color(0xFFD88B76),
                        iconBackground = Color(0xFFF3E3D3),
                        isNext = true
                    )
                }

                // Completed Doses (show up to 2 most recent)
                completedDoses.take(2).forEach { dose ->
                    JourneyTimelineItem(
                        icon = Icons.Outlined.CheckCircle,
                        title = dose.medicineName,
                        subtitle = "Completed",
                        iconTint = Color(0xFF7BA99E),
                        iconBackground = Color(0xFFE5F0ED),
                        isNext = false
                    )
                }
                
                if (taken > 2) {
                    Text(
                        text = "+ ${taken - 2} more completed",
                        fontSize = 13.sp,
                        color = ZariyaTextTertiary,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyTimelineItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    iconBackground: Color,
    isNext: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isNext) ZariyaTextPrimary else ZariyaTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = ZariyaTextSecondary
            )
        }
    }
}
