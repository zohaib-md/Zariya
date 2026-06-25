package com.project.zariya.feature.analytics.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.LoadingState
import com.project.zariya.core.ui.theme.*
import com.project.zariya.core.util.DateTimeUtils
import com.project.zariya.feature.analytics.domain.model.DailyAdherence
import com.project.zariya.feature.analytics.domain.model.Period
import kotlinx.coroutines.delay

// ── Gradient constants ──────────────────────────────────────────────────
private val TealCyan = listOf(Color(0xFFC9938A), Color(0xFFD4A59A))
private val GradientBrush = Brush.linearGradient(TealCyan)
private val GlassColor = Color(0xFFFFFFFF)
private val GlassBorder = Brush.linearGradient(
    listOf(Color(0xFFC9938A).copy(alpha = 0.35f), Color(0xFFD4A59A).copy(alpha = 0.12f))
)

// ── Main Screen ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZariyaBackground)
    ) {
        // ── Top App Bar ──
        TopAppBar(
            title = {
                Text(
                    text = "Analytics",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = GradientBrush,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ZariyaTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ── Period Selector ──
                PeriodSelector(
                    selected = uiState.selectedPeriod,
                    onSelect = { viewModel.selectPeriod(it) }
                )

                // ── Hero Stats Card (Donut) ──
                uiState.todayStats?.let { stats ->
                    HeroStatsCard(
                        percentage = stats.adherencePercentage,
                        taken = stats.takenDoses,
                        missed = stats.missedDoses,
                        total = stats.totalDoses
                    )
                }

                // ── Weekly Bar Chart ──
                uiState.weeklyReport?.let { report ->
                    SectionEntry(delayMs = 150) {
                        GlassCard {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Weekly Overview",
                                    style = androidx.compose.ui.text.TextStyle(
                                        brush = GradientBrush,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                WeeklyBarChart(dailyStats = report.dailyStats)
                            }
                        }
                    }
                }

                // ── Monthly Heatmap ──
                if (uiState.monthlyAdherence.isNotEmpty()) {
                    SectionEntry(delayMs = 300) {
                        GlassCard {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Monthly Heatmap",
                                    style = androidx.compose.ui.text.TextStyle(
                                        brush = GradientBrush,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                MonthlyHeatmap(dailyStats = uiState.monthlyAdherence)
                            }
                        }
                    }
                }

                // ── Streak Card ──
                uiState.todayStats?.let { stats ->
                    if (stats.streakDays > 0) {
                        SectionEntry(delayMs = 450) {
                            StreakCard(streakDays = stats.streakDays)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// ── Glass Card ──────────────────────────────────────────────────────────
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassColor)
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        content()
    }
}

// ── Staggered entrance animation ────────────────────────────────────────
@Composable
private fun SectionEntry(
    delayMs: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = 1
    }
    AnimatedVisibility(
        visible = visible == 1,
        enter = fadeIn(tween(500)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { it / 4 }
        )
    ) {
        content()
    }
}

// ── Period Selector ─────────────────────────────────────────────────────
@Composable
private fun PeriodSelector(selected: Period, onSelect: (Period) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Period.entries.forEach { period ->
            val isSelected = selected == period
            val bgAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = tween(350),
                label = "chip_bg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSelected) Modifier.background(GradientBrush)
                        else Modifier.background(ZariyaSurfaceElevated)
                    )
                    .then(
                        if (!isSelected) Modifier.border(
                            1.dp,
                            ZariyaPrimary.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        ) else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(period) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.displayName,
                    color = if (isSelected) ZariyaTextOnPrimary else ZariyaTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ── Hero Stats Card ─────────────────────────────────────────────────────
@Composable
private fun HeroStatsCard(percentage: Float, taken: Int, missed: Int, total: Int) {
    val progress = (percentage / 100f).coerceIn(0f, 1f)
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatable.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }
    val animatedValue = animatable.value

    // Shimmer sweep
    val infiniteTransition = rememberInfiniteTransition(label = "hero_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    SectionEntry(delayMs = 0) {
        GlassCard(
            modifier = Modifier.drawBehind {
                // subtle shimmer sweep
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerOffset, 0f),
                        end = Offset(shimmerOffset + 300f, size.height)
                    )
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Donut Ring ──
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(180.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        // Track
                        drawArc(
                            color = ZariyaSurfaceElevated,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Gradient arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                0f to Color(0xFFC9938A),
                                0.5f to Color(0xFFD4A59A),
                                1f to Color(0xFFC9938A)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedValue,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Glow dot at end
                        if (animatedValue > 0.01f) {
                            val angle = Math.toRadians((-90.0 + 360.0 * animatedValue))
                            val radius = (size.minDimension / 2f) - strokeWidth / 2f
                            val cx = center.x + radius * kotlin.math.cos(angle).toFloat()
                            val cy = center.y + radius * kotlin.math.sin(angle).toFloat()
                            drawCircle(
                                color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                                radius = strokeWidth * 1.2f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = strokeWidth * 0.35f,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // Center text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedValue * 100).toInt()}%",
                            color = ZariyaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                        Text(
                            text = "adherence",
                            color = ZariyaTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Mini stat pills ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniStatPill(
                        label = "Taken",
                        value = taken,
                        icon = "✓",
                        color = ZariyaSuccess
                    )
                    MiniStatPill(
                        label = "Missed",
                        value = missed,
                        icon = "✗",
                        color = ZariyaError
                    )
                    MiniStatPill(
                        label = "Total",
                        value = total,
                        icon = "●",
                        color = ZariyaPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStatPill(label: String, value: Int, icon: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "$value",
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = color.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
    }
}

// ── Weekly Bar Chart ────────────────────────────────────────────────────
@Composable
private fun WeeklyBarChart(dailyStats: List<DailyAdherence>) {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val barCount = dailyStats.size.coerceAtMost(7)

    // Animate each bar height independently with spring
    val animatables = remember(barCount) {
        List(barCount) { Animatable(0f) }
    }
    LaunchedEffect(dailyStats) {
        dailyStats.take(7).forEachIndexed { index, stat ->
            val target = (stat.percentage / 100f).coerceIn(0f, 1f)
            delay(80L)
            animatables[index].animateTo(
                target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (barCount == 0) return@Canvas
        val barWidth = size.width / (barCount * 2.2f)
        val maxHeight = size.height - 8.dp.toPx()
        val spacing = size.width / barCount

        // Subtle grid lines
        for (i in 1..4) {
            val y = maxHeight * (1f - i / 4f)
            drawLine(
                color = ZariyaSurfaceElevated.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        dailyStats.take(7).forEachIndexed { index, stat ->
            val animatedHeight = animatables[index].value * maxHeight
            val centerX = spacing * index + spacing / 2f
            val left = centerX - barWidth / 2f

            // Background bar
            drawRoundRect(
                color = ZariyaSurfaceElevated.copy(alpha = 0.4f),
                topLeft = Offset(left, 0f),
                size = Size(barWidth, maxHeight),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            // Value bar with gradient
            val barColor = when {
                stat.percentage >= 80f -> Brush.verticalGradient(
                    listOf(Color(0xFF6AADA0), Color(0xFF7BA99E)) // Sage Teal
                )
                stat.percentage >= 50f -> Brush.verticalGradient(
                    listOf(Color(0xFFE6D0A5), Color(0xFFDCA9A1)) // Pastel Yellow/Orange
                )
                stat.percentage > 0f -> Brush.verticalGradient(
                    listOf(Color(0xFFC9938A), Color(0xFFB07D74)) // Warm Rose
                )
                else -> Brush.verticalGradient(
                    listOf(ZariyaSurfaceElevated, ZariyaSurfaceElevated)
                )
            }
            if (animatedHeight > 0f) {
                drawRoundRect(
                    brush = barColor,
                    topLeft = Offset(left, maxHeight - animatedHeight),
                    size = Size(barWidth, animatedHeight),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Day labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayLabels.take(barCount).forEach { label ->
            Text(
                text = label,
                color = ZariyaTextTertiary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

// ── Monthly Heatmap ─────────────────────────────────────────────────────
@Composable
private fun MonthlyHeatmap(dailyStats: List<DailyAdherence>) {
    val rows = (dailyStats.size + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    if (index < dailyStats.size) {
                        val stat = dailyStats[index]
                        val intensity = (stat.percentage / 100f).coerceIn(0f, 1f)

                        // Animate fade-in
                        val alpha = remember { Animatable(0f) }
                        LaunchedEffect(index) {
                            delay(index * 30L)
                            alpha.animateTo(1f, animationSpec = tween(400))
                        }

                        val cellColor = Color(0xFFC9938A).copy(alpha = (intensity * 0.85f).coerceAtLeast(0.06f))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(cellColor.copy(alpha = cellColor.alpha * alpha.value)),
                            contentAlignment = Alignment.Center
                        ) {
                            val dayText = DateTimeUtils.run { stat.date.formatShortDate() }.take(2)
                            Text(
                                text = dayText,
                                color = ZariyaTextPrimary.copy(alpha = 0.85f * alpha.value),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Less", color = ZariyaTextTertiary, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(6.dp))
            listOf(0.1f, 0.3f, 0.55f, 0.8f, 1f).forEach { level ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF00BFA6).copy(alpha = level * 0.85f))
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text("More", color = ZariyaTextTertiary, fontSize = 10.sp)
        }
    }
}

// ── Streak Card ─────────────────────────────────────────────────────────
@Composable
private fun StreakCard(streakDays: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Animate counter
    val counterAnimatable = remember { Animatable(0f) }
    LaunchedEffect(streakDays) {
        counterAnimatable.animateTo(
            streakDays.toFloat(),
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fire icon with glow
            Box(contentAlignment = Alignment.Center) {
                // Glow background
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFF9800).copy(alpha = glowAlpha * 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Text(
                    text = "🔥",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${counterAnimatable.value.toInt()}",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFFFAB40), Color(0xFFFF6D00))
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "day streak!",
                        color = ZariyaTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (streakDays >= 7) "Incredible consistency! Keep it up! 🌟"
                    else if (streakDays >= 3) "Great momentum! You're building a habit 💪"
                    else "Every day counts. Stay strong! ✨",
                    color = ZariyaTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}
