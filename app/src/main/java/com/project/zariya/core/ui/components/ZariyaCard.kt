package com.project.zariya.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.project.zariya.core.ui.theme.ZariyaGlassBorder
import com.project.zariya.core.ui.theme.ZariyaGlassSurface
import com.project.zariya.core.ui.theme.ZariyaGradientEnd
import com.project.zariya.core.ui.theme.ZariyaGradientStart
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaSurfaceElevated

private val GlassCardShape = RoundedCornerShape(20.dp)

@Composable
fun ZariyaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isElevated: Boolean = false,
    gradientBorder: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 400f
        ),
        label = "cardScale"
    )

    val cardModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(GlassCardShape)
        .then(
            if (isElevated) {
                Modifier.drawBehind {
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.04f),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        size = size.copy(height = size.height + 6.dp.toPx()),
                        topLeft = Offset(0f, 3.dp.toPx())
                    )
                }
            } else Modifier
        )
        .then(
            if (gradientBorder) {
                Modifier.border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(ZariyaGradientStart, ZariyaGradientEnd)
                    ),
                    shape = GlassCardShape
                )
            } else {
                Modifier.border(
                    width = 1.dp,
                    color = ZariyaGlassBorder,
                    shape = GlassCardShape
                )
            }
        )
        .background(ZariyaSurface, GlassCardShape)

    if (onClick != null) {
        Box(
            modifier = cardModifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
        ) {
            Column(content = content)
        }
    } else {
        Box(modifier = cardModifier) {
            Column(content = content)
        }
    }
}
