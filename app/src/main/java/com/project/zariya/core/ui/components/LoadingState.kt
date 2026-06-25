package com.project.zariya.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.project.zariya.core.ui.theme.ZariyaGradientEnd
import com.project.zariya.core.ui.theme.ZariyaGradientStart
import com.project.zariya.core.ui.theme.ZariyaShimmerBase
import com.project.zariya.core.ui.theme.ZariyaShimmerHighlight
import com.project.zariya.core.ui.theme.ZariyaShapes

private val ShimmerCardShape = RoundedCornerShape(20.dp)

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    // Fade-in effect
    val fadeIn by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerFade"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            ZariyaShimmerBase,
            ZariyaShimmerHighlight,
            ZariyaGradientStart.copy(alpha = 0.06f),
            ZariyaShimmerHighlight,
            ZariyaShimmerBase
        ),
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer { alpha = fadeIn },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card shimmer 1 - Large card
        ShimmerCard(brush = shimmerBrush, height = 120)

        // Card shimmer 2 - Medium card with row elements
        ShimmerCardWithDetails(brush = shimmerBrush)

        // Card shimmer 3 - Compact card
        ShimmerCard(brush = shimmerBrush, height = 88)
    }
}

@Composable
private fun ShimmerCard(brush: Brush, height: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(ShimmerCardShape)
            .background(brush)
    )
}

@Composable
private fun ShimmerCardWithDetails(brush: Brush) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShimmerCardShape)
            .background(ZariyaShimmerBase)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(brush)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title line
                ShimmerItem(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(14.dp),
                    brush = brush
                )
                // Subtitle line
                ShimmerItem(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp),
                    brush = brush
                )
            }
        }

        // Content lines
        ShimmerItem(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(10.dp),
            brush = brush
        )
        ShimmerItem(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(10.dp),
            brush = brush
        )
    }
}

@Composable
fun ShimmerItem(modifier: Modifier = Modifier, brush: Brush) {
    Spacer(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(brush)
    )
}
