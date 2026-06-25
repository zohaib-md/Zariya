package com.project.zariya.core.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary - Warm Rose
val ZariyaPrimary = Color(0xFFC9938A)
val ZariyaPrimaryVariant = Color(0xFFB07D74)
val ZariyaPrimaryContainer = Color(0xFFF5E6E2)
val ZariyaPrimaryDark = Color(0xFF9E655C)

// Secondary - Sage Teal (softer healthcare feel)
val ZariyaSecondary = Color(0xFF7BA99E)
val ZariyaSecondaryContainer = Color(0xFFE8F0EE)

// Background & Surface - Warm Cream
val ZariyaBackground = Color(0xFFFAF8F5)
val ZariyaSurface = Color(0xFFFFFFFF)
val ZariyaSurfaceVariant = Color(0xFFF0EDE8)
val ZariyaSurfaceElevated = Color(0xFFF5F2EE)
val ZariyaSurfaceBright = Color(0xFFE8E4DF)

// Semantic (Softer for light theme)
val ZariyaSuccess = Color(0xFF6AADA0) // Using sage teal as success
val ZariyaSuccessContainer = Color(0xFFE8F0EE)
val ZariyaWarning = Color(0xFFD4A59A) // Using warm rose as warning
val ZariyaWarningContainer = Color(0xFFF5E6E2)
val ZariyaError = Color(0xFFC9938A) // Same warm rose for errors to keep it gentle
val ZariyaErrorContainer = Color(0xFFF5E6E2)
val ZariyaInfo = Color(0xFF8BA5B5) // Muted blue

// Severity badges (Pastel)
val SeverityMild = Color(0xFFB4D4CC)
val SeverityModerate = Color(0xFFE6D0A5)
val SeveritySevere = Color(0xFFDCA9A1)

// Text
val ZariyaTextPrimary = Color(0xFF2C2C2C)
val ZariyaTextSecondary = Color(0xFF7A7A7A)
val ZariyaTextTertiary = Color(0xFFA0A0A0)
val ZariyaTextOnPrimary = Color(0xFFFFFFFF)

// Medicine Form Colors (Pastel)
val TabletColor = Color(0xFFA5B4D4)
val CapsuleColor = Color(0xFFCCB4D4)
val SyrupColor = Color(0xFFB4D4CC)
val InjectionColor = Color(0xFFDCA9A1)
val DropsColor = Color(0xFFA5C5D4)
val InhalerColor = Color(0xFFC0C8CC)
val CreamColor = Color(0xFFE6E2A5)
val PatchColor = Color(0xFFC0B4B0)

// ─── Premium Gradient System ───────────────────────────────────────────
val ZariyaGradientStart = Color(0xFFC9938A)
val ZariyaGradientEnd = Color(0xFFD4A59A)
val ZariyaGradientAccent = Color(0xFF7BA99E)

// Glass-morphism / Shadows
val ZariyaGlassBorder = Color(0xFFE8E4DF)
val ZariyaGlassSurface = Color(0xFFFFFFFF)

// Shimmer
val ZariyaShimmerBase = Color(0xFFF0EDE8)
val ZariyaShimmerHighlight = Color(0xFFF5F2EE)

// Pre-built gradient brush (warm rose)
val ZariyaGradient: Brush = Brush.linearGradient(
    colors = listOf(ZariyaGradientStart, ZariyaGradientEnd)
)

// Horizontal gradient brush
val ZariyaGradientHorizontal: Brush = Brush.horizontalGradient(
    colors = listOf(ZariyaGradientStart, ZariyaGradientEnd)
)

// Accent gradient brush (rose → sage)
val ZariyaGradientAccentBrush: Brush = Brush.linearGradient(
    colors = listOf(ZariyaGradientStart, ZariyaGradientAccent)
)

// Semantic gradients
val ZariyaSuccessGradient: Brush = Brush.horizontalGradient(
    colors = listOf(Color(0xFF6AADA0), Color(0xFF7BA99E))
)
val ZariyaErrorGradient: Brush = Brush.horizontalGradient(
    colors = listOf(Color(0xFFB07D74), Color(0xFFC9938A))
)
val ZariyaWarningGradient: Brush = Brush.horizontalGradient(
    colors = listOf(Color(0xFFD4A59A), Color(0xFFDCA9A1))
)
val ZariyaNeutralGradient: Brush = Brush.horizontalGradient(
    colors = listOf(Color(0xFFA0A0A0), Color(0xFFB8B8B8))
)
