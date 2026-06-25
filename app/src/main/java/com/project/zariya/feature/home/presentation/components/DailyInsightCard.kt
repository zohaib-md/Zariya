package com.project.zariya.feature.home.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.core.ui.icons.ZariyaIcons
import com.project.zariya.feature.home.domain.model.HomeInsight
import com.project.zariya.feature.home.domain.model.InsightPriority

@Composable
fun DailyInsightCard(
    insight: HomeInsight?,
    modifier: Modifier = Modifier
) {
    if (insight == null) return

    val (bgGradient, iconTint, iconBg) = when (insight.priority) {
        InsightPriority.CRITICAL -> Triple(
            listOf(Color(0xFFFFF0F0), Color(0xFFFFE6E6)),
            Color(0xFFE57373),
            Color(0xFFFFCDD2)
        )
        InsightPriority.HIGH -> Triple(
            listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)),
            Color(0xFFFFB300),
            Color(0xFFFFE082)
        )
        InsightPriority.MEDIUM -> Triple(
            listOf(Color(0xFFE5F0ED), Color(0xFFCDE2DB)),
            Color(0xFF7BA99E), // Sage Teal
            Color(0xFFA5C9C0)
        )
        InsightPriority.LOW -> Triple(
            listOf(Color(0xFFF5F2EE), Color(0xFFEBE6DF)),
            Color(0xFFD88B76), // Terracotta
            Color(0xFFF3E3D3)
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(bgGradient))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Outlined.Analytics, // Fallback icon for now
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Insight Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2220)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = insight.message,
                fontSize = 14.sp,
                color = Color(0xFF5A4D4A),
                lineHeight = 20.sp
            )
        }
    }
}
