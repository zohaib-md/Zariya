package com.project.zariya.feature.home.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import com.project.zariya.core.ui.theme.ZariyaTextTertiary

@Composable
fun AnimatedAdherenceCard(
    taken: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) (taken.toFloat() / total.toFloat()) else 0f
    val displayPercentage = (percentage * 100).toInt()

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = percentage,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val arcStartAngle = 140f
    val arcSweepAngle = 260f

    val statusText = when {
        displayPercentage == 100 -> "EXCELLENT"
        displayPercentage >= 75 -> "VERY GOOD"
        displayPercentage >= 50 -> "GOOD"
        else -> "NEEDS ATTENTION"
    }
    val statusColor = when {
        displayPercentage >= 75 -> Color(0xFF7BA99E) // Sage Teal
        displayPercentage >= 50 -> Color(0xFFD88B76) // Terracotta
        else -> Color(0xFFE57373) // Red
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ZariyaSurface)
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Today's Adherence",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ZariyaTextPrimary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 16.dp.toPx()
                
                // Background Track
                drawArc(
                    color = Color(0xFFEBE6DF),
                    startAngle = arcStartAngle,
                    sweepAngle = arcSweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )

                // Foreground Gradient Track
                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFD4A59A), // Light Warm Rose
                        Color(0xFFC9938A), // Deep Rose
                        Color(0xFFA5B4D4), // Muted Blue/Purple
                        Color(0xFFD4A59A)
                    ),
                    center = Offset(size.width / 2, size.height / 2)
                )

                drawArc(
                    brush = gradientBrush,
                    startAngle = arcStartAngle,
                    sweepAngle = arcSweepAngle * animatedProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
            }

            // Inner Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                if (displayPercentage >= 75) {
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = null,
                        tint = Color(0xFFD4A59A),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Top Tier",
                        fontSize = 12.sp,
                        color = ZariyaTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = "$displayPercentage%",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E2220),
                    letterSpacing = (-2).sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = ZariyaTextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Min/Max Labels
        Row(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0%", fontSize = 12.sp, color = ZariyaTextTertiary, fontWeight = FontWeight.Medium)
            Text(text = "100%", fontSize = 12.sp, color = ZariyaTextTertiary, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "$taken out of $total doses completed",
            fontSize = 14.sp,
            color = ZariyaTextSecondary
        )
    }
}

