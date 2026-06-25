package com.project.zariya.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.core.ui.theme.ZariyaPrimaryContainer
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import java.util.Calendar

// Exact logo pill colors
private val PillTerracotta = Color(0xFFD88B76)
private val PillCream = Color(0xFFE8D0BC)
private val PillDivider = Color(0x592E2220) // #2E2220 at 35% opacity

private val dailyReflections = listOf(
    "One dose at a time.",
    "Small habits. Better health.",
    "Your care companion.",
    "Consistency creates wellness.",
    "Today's progress matters.",
    "A gentle reminder to heal.",
    "Health is a daily practice.",
    "Every dose is self-care.",
    "You're doing great today.",
    "Wellness starts with you.",
    "Be kind to your body.",
    "Trust the process.",
    "Little steps, big difference.",
    "Your health journey, simplified.",
    "Taking care, one day at a time.",
    "Strength in every small step.",
    "Nourish your body, nurture your soul.",
    "Healing is not linear.",
    "You showed up today. That counts.",
    "Progress, not perfection.",
    "Rest is part of the journey.",
    "Your well-being matters most.",
    "Grace in every dose.",
    "Breathe. You've got this.",
    "Caring for yourself is never selfish.",
    "Today is a fresh start.",
    "Gentle with yourself, always.",
    "One moment of care at a time.",
    "You are your best investment.",
    "Keep going. You're closer than you think.",
    "Wellness is a journey, not a destination."
)

@Composable
fun ZariyaTopBar(
    modifier: Modifier = Modifier
) {
    val todayReflection = remember {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        dailyReflections[dayOfYear % dailyReflections.size]
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ZariyaPrimaryContainer)
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 14.dp)
    ) {
        // Pill-shaped logo with "zariya" inside
        PillLogo()

        Spacer(modifier = Modifier.height(6.dp))

        // Daily reflection — subtle, elegant
        Text(
            text = todayReflection,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = ZariyaTextSecondary,
            letterSpacing = 0.3.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
private fun PillLogo() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                val radius = h / 2

                // Left half — Terracotta
                val leftPath = Path().apply {
                    moveTo(w / 2, 0f)
                    lineTo(radius, 0f)
                    arcTo(
                        rect = Rect(
                            left = 0f,
                            top = 0f,
                            right = h,
                            bottom = h
                        ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    lineTo(w / 2, h)
                    close()
                }
                drawPath(leftPath, PillTerracotta)

                // Right half — Cream
                val rightPath = Path().apply {
                    moveTo(w / 2, 0f)
                    lineTo(w - radius, 0f)
                    arcTo(
                        rect = Rect(
                            left = w - h,
                            top = 0f,
                            right = w,
                            bottom = h
                        ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(w / 2, h)
                    close()
                }
                drawPath(rightPath, PillCream)

                // Divider line
                drawLine(
                    color = PillDivider,
                    start = Offset(w / 2, 0f),
                    end = Offset(w / 2, h),
                    strokeWidth = 1.5f
                )
            }
            .padding(horizontal = 20.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "zariya",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E2220),
            letterSpacing = 3.sp
        )
    }
}
