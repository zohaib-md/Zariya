package com.project.zariya.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.core.ui.icons.ZariyaIcons
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary

@Composable
fun RichInventoryWidget(
    lowStockCount: Int,
    totalTracked: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHealthy = lowStockCount == 0
    val bgTint = if (isHealthy) Color(0xFFE5F0ED) else Color(0xFFFFE6E6)
    val iconTint = if (isHealthy) Color(0xFF7BA99E) else Color(0xFFE57373)
    
    val statusTitle = if (isHealthy) "Healthy" else "Refill Needed"
    val statusSubtitle = if (isHealthy) "No low stock" else "$lowStockCount items running low"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ZariyaSurface)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Icon Box
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Inventory Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Medication Inventory",
                fontSize = 14.sp,
                color = ZariyaTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ZariyaTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = statusSubtitle,
                fontSize = 14.sp,
                color = iconTint,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Chevron/Indicator could go here if needed
    }
}
