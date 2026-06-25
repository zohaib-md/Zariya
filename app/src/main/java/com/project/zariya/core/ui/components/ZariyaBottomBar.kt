package com.project.zariya.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.project.zariya.core.navigation.Route
import com.project.zariya.core.ui.icons.ZariyaIcons
import com.project.zariya.core.ui.theme.ZariyaGradientStart
import com.project.zariya.core.ui.theme.ZariyaPrimary
import com.project.zariya.core.ui.theme.ZariyaTextOnPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary

private val GradientBrush = Brush.horizontalGradient(
    colors = listOf(Color(0xFFC9938A), Color(0xFFD4A59A))
)

@Composable
fun ZariyaBottomBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(percent = 50),
                    spotColor = ZariyaGradientStart.copy(alpha = 0.25f),
                    ambientColor = ZariyaGradientStart.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.White)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            BottomBarItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentRoute == Route.Home.route,
                onClick = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Medicines
            BottomBarItem(
                icon = ZariyaIcons.PillBottle,
                label = "Meds",
                isSelected = currentRoute == Route.MedicineList.route,
                onClick = {
                    navController.navigate(Route.MedicineList.route) {
                        popUpTo(Route.Home.route)
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.weight(1f)
            )



            // Analytics
            BottomBarItem(
                icon = ZariyaIcons.Capsule,
                label = "Stats",
                isSelected = currentRoute == Route.Analytics.route,
                onClick = {
                    navController.navigate(Route.Analytics.route) {
                        popUpTo(Route.Home.route)
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Profile
            BottomBarItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentRoute == Route.ProfileList.route,
                onClick = {
                    navController.navigate(Route.ProfileList.route) {
                        popUpTo(Route.Home.route)
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "iconScale"
    )

    val iconBackgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "iconBackgroundAlpha"
    )
    
    val iconOffsetY by animateFloatAsState(
        targetValue = if (isSelected) -4f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "iconOffsetY"
    )

    val iconTint = if (isSelected) Color.White else ZariyaTextSecondary
    val labelColor = if (isSelected) ZariyaPrimary else ZariyaTextSecondary

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = iconOffsetY.dp)
                .size(36.dp)
                .background(
                    color = ZariyaPrimary.copy(alpha = iconBackgroundAlpha),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}
