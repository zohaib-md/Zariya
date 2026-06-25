package com.project.zariya.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.core.ui.theme.ZariyaGradientEnd
import com.project.zariya.core.ui.theme.ZariyaGradientStart
import com.project.zariya.core.ui.theme.ZariyaPrimary
import com.project.zariya.core.ui.theme.ZariyaTextOnPrimary
import com.project.zariya.core.ui.theme.ZariyaTextPrimary

private val ButtonShape = RoundedCornerShape(14.dp)

private val GradientBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFC9938A),
        Color(0xFFD4A59A)
    )
)

private val GradientBorderBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFC9938A).copy(alpha = 0.40f),
        Color(0xFFD4A59A).copy(alpha = 0.40f)
    )
)

@Composable
fun ZariyaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "primaryBtnScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.0f else 0.15f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "primaryBtnGlow"
    )

    val resolvedEnabled = enabled && !isLoading

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (resolvedEnabled) 1f else 0.5f
            }
            .drawBehind {
                if (resolvedEnabled) {
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.08f),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        size = size.copy(height = size.height + 4.dp.toPx()),
                        topLeft = Offset(0f, 2.dp.toPx())
                    )
                }
            }
            .height(52.dp)
            .clip(ButtonShape)
            .background(
                brush = if (resolvedEnabled) GradientBrush
                else Brush.horizontalGradient(
                    colors = listOf(
                        ZariyaGradientStart.copy(alpha = 0.4f),
                        ZariyaGradientEnd.copy(alpha = 0.4f)
                    )
                ),
                shape = ButtonShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = resolvedEnabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = ZariyaTextOnPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ZariyaTextOnPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = ZariyaTextOnPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

@Composable
fun ZariyaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "secondaryBtnScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .height(52.dp)
            .clip(ButtonShape)
            .border(
                width = 1.5.dp,
                brush = GradientBorderBrush,
                shape = ButtonShape
            )
            .background(Color.Transparent, ButtonShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ZariyaGradientStart
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = ZariyaGradientStart,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun ZariyaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "textBtnScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .clip(ButtonShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            letterSpacing = 0.3.sp,
            color = ZariyaGradientStart
        )
    }
}
