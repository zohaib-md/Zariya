package com.project.zariya.feature.home.presentation

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.LoadingState
import com.project.zariya.core.ui.components.ZariyaTopBar
import com.project.zariya.core.ui.icons.ZariyaIcons
import com.project.zariya.feature.home.presentation.components.*
import com.project.zariya.core.ui.theme.*
import com.project.zariya.core.util.DateTimeUtils
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import kotlinx.coroutines.delay

// ─── Gradient Palette (Premium Light Theme) ──────────────────────────────
private val GradientTealCyan = listOf(Color(0xFFC9938A), Color(0xFFD4A59A)) // Warm Rose
private val GradientAmberRed = listOf(Color(0xFFD4A59A), Color(0xFFDCA9A1)) // Soft Peach
private val GradientPurple = listOf(Color(0xFFA5B4D4), Color(0xFFCCB4D4)) // Muted Blue/Purple
private val GradientPink = listOf(Color(0xFFC9938A), Color(0xFFB07D74)) // Deep Rose
private val GradientBlue = listOf(Color(0xFF7BA99E), Color(0xFF6AADA0)) // Sage Teal
private val GradientCelebration = listOf(Color(0xFFC9938A), Color(0xFF7BA99E), Color(0xFFD4A59A))
private val SurfaceGlass = Color(0xFFFFFFFF)
private val SurfaceGlassLight = Color(0xFFF5F2EE)

// ─── Main HomeScreen ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMedicines: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToInteractions: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToForHer: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedDoseLog by remember { mutableStateOf<com.project.zariya.feature.reminder.domain.model.DoseLog?>(null) }
    var showDoseActionSheet by remember { mutableStateOf(false) }

    // Staggered entrance control
    var sectionsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            sectionsVisible = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZariyaBackground)
    ) {
        ZariyaTopBar()

        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(top = 32.dp))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ── Greeting Section ──
                val userName = uiState.activeProfile?.name ?: ""
                if (userName.isNotEmpty()) {
                    StaggeredSection(visible = sectionsVisible, index = 0) {
                        GreetingSection(userName = userName)
                    }
                }

                // ── Section 1: Hero Medication Journey ──
                StaggeredSection(visible = sectionsVisible, index = 1) {
                    HeroMedicationJourney(todayLogs = uiState.todayLogs)
                }

                // ── Section 2: Animated Adherence ──
                StaggeredSection(visible = sectionsVisible, index = 2) {
                    AnimatedAdherenceCard(
                        taken = uiState.takenToday,
                        total = uiState.totalToday
                    )
                }

                // ── Section 3: Daily Insight ──
                if (uiState.currentInsight != null) {
                    StaggeredSection(visible = sectionsVisible, index = 3) {
                        DailyInsightCard(insight = uiState.currentInsight)
                    }
                }

                // ── Section 4: Rich Inventory Widget ──
                StaggeredSection(visible = sectionsVisible, index = 4) {
                    RichInventoryWidget(
                        lowStockCount = uiState.lowStockItems.size,
                        totalTracked = uiState.medicines.count { it.isStockTracked },
                        onClick = onNavigateToInventory
                    )
                }

                // ── Section 5: Quick Actions Grid ──
                StaggeredSection(visible = sectionsVisible, index = 5) {
                    QuickActionsGrid(
                        onNavigateToReminders = onNavigateToReminders,
                        onNavigateToMedicines = onNavigateToMedicines,
                        onNavigateToScanner = onNavigateToScanner,
                        onNavigateToInteractions = onNavigateToInteractions,
                        onNavigateToHealth = onNavigateToHealth,
                        onNavigateToAnalytics = onNavigateToAnalytics
                    )
                }

                // ── Section 6: Today's Activity Timeline ──
                if (uiState.todayLogs.isNotEmpty()) {
                    StaggeredSection(visible = sectionsVisible, index = 6) {
                        TodayActivityTimeline(
                            logs = uiState.todayLogs,
                            onLogClick = { log ->
                                selectedDoseLog = log
                                showDoseActionSheet = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showDoseActionSheet && selectedDoseLog != null) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showDoseActionSheet = false },
            containerColor = ZariyaSurface,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(color = ZariyaTextTertiary) }
        ) {
            DoseActionSheetContent(
                log = selectedDoseLog!!,
                onUpdateStatus = { status ->
                    viewModel.onDoseStatusUpdate(selectedDoseLog!!, status)
                    showDoseActionSheet = false
                },
                onCancel = { showDoseActionSheet = false }
            )
        }
    }
}

@Composable
private fun GreetingSection(
    userName: String,
    modifier: Modifier = Modifier
) {
    val dateText = remember {
        java.text.SimpleDateFormat("EEEE, dd MMM yyyy", java.util.Locale.US)
            .format(java.util.Date()).uppercase()
    }
    
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val isNight = currentHour < 5 || currentHour > 16
    val greetingText = when (currentHour) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..20 -> "Good evening,"
        else -> "Good night,"
    }
    val timeLabel = when (currentHour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..20 -> "Evening"
        else -> "Night"
    }
    val icon = if (isNight) androidx.compose.material.icons.Icons.Outlined.DarkMode else androidx.compose.material.icons.Icons.Outlined.LightMode
    
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        // Top row: Date and Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = ZariyaTextTertiary
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFD4A59A),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = timeLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD4A59A)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Greeting and Name
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = greetingText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Normal,
                color = ZariyaTextSecondary,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$userName.",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ZariyaTextPrimary,
                letterSpacing = (-0.5).sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gradient underline accent
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(1.5.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFD4A59A),
                            Color(0xFFD4A59A).copy(alpha = 0f)
                        )
                    )
                )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DoseActionSheetContent(
    log: com.project.zariya.feature.reminder.domain.model.DoseLog,
    onUpdateStatus: (DoseStatus) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Update Status",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ZariyaTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${log.medicineName} at ${DateTimeUtils.run { log.scheduledTime.formatTime() }}",
            fontSize = 15.sp,
            color = ZariyaTextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        DoseActionButton(
            label = "Mark as Taken",
            color = ZariyaSuccess,
            icon = Icons.Outlined.Medication,
            onClick = { onUpdateStatus(DoseStatus.TAKEN) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        DoseActionButton(
            label = "Mark as Skipped",
            color = ZariyaWarning,
            icon = Icons.Outlined.Notifications, // You might want an 'alarm_off' icon here if available
            onClick = { onUpdateStatus(DoseStatus.SKIPPED) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        DoseActionButton(
            label = "Reset to Pending",
            color = ZariyaTextTertiary,
            icon = Icons.Outlined.Schedule,
            onClick = { onUpdateStatus(DoseStatus.PENDING) }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun DoseActionButton(
    label: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

// ─── Staggered Section Wrapper ──────────────────────────────────────────────────
@Composable
private fun StaggeredSection(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    var sectionVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(index * 150L)
            sectionVisible = true
        }
    }

    AnimatedVisibility(
        visible = sectionVisible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    ) {
        content()
    }
}

// ─── Section 1: Hero Adherence Card ─────────────────────────────────────────────
@Composable
private fun HeroAdherenceCard(taken: Int, total: Int) {
    val progress = if (total > 0) taken.toFloat() / total.toFloat() else 0f

    // Animated progress value
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }

    // Animated percentage counter
    var displayPercent by remember { mutableIntStateOf(0) }
    LaunchedEffect(progress) {
        val target = (progress * 100).toInt()
        for (i in 0..target) {
            displayPercent = i
            delay(12L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                // Soft drop shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.06f),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = size.copy(height = size.height + 4.dp.toPx()),
                    topLeft = Offset(0f, 2.dp.toPx())
                )
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ZariyaSurface,
                        ZariyaSurfaceElevated
                    )
                ),
                RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress ring
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    // Background ring
                    drawArc(
                        color = ZariyaSurfaceVariant,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc — warm rose gradient
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                ZariyaGradientStart,
                                ZariyaGradientEnd,
                                ZariyaGradientStart
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$displayPercent%",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZariyaTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                Text(
                    text = "Today's Adherence",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZariyaTextPrimary,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$taken of $total doses taken",
                    fontSize = 14.sp,
                    color = ZariyaTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Mini progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ZariyaSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress.value)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(GradientTealCyan)
                            )
                    )
                }
            }
        }
    }
}

// ─── Section 2: Next Dose Card ──────────────────────────────────────────────────
@Composable
private fun NextDoseCard(
    reminder: com.project.zariya.feature.reminder.domain.model.Reminder?,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "next_dose")

    // Pulse for pill icon
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .drawBehind {
                // Soft shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.05f),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    size = size.copy(height = size.height + 3.dp.toPx()),
                    topLeft = Offset(0f, 2.dp.toPx())
                )
            }
            .background(
                if (reminder != null)
                    Brush.horizontalGradient(
                        colors = listOf(
                            ZariyaPrimaryContainer,
                            ZariyaSurface
                        )
                    )
                else
                    Brush.horizontalGradient(
                        colors = listOf(
                            ZariyaSecondaryContainer,
                            ZariyaSurface
                        )
                    ),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        if (reminder != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pulsing pill icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(ZariyaPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ZariyaIcons.PillBottle,
                        contentDescription = null,
                        tint = ZariyaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEXT DOSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZariyaTextTertiary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.medicineName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZariyaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${reminder.dosage} · ${reminder.medicineForm}",
                        fontSize = 13.sp,
                        color = ZariyaTextSecondary
                    )
                }

                // Countdown
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = DateTimeUtils.formatCountdown(reminder.nextTriggerTime),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZariyaPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "remaining",
                        fontSize = 11.sp,
                        color = ZariyaTextTertiary
                    )
                }
            }
        } else {
            // All caught up state
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "All caught up! ✨",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ZariyaSecondary
                )
            }
        }
    }
}

// ─── Section 3: Quick Stats Row ─────────────────────────────────────────────────
@Composable
private fun QuickStatsRow(
    medicineCount: Int,
    reminderCount: Int,
    visible: Boolean,
    onNavigateToMedicines: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatMiniCard(
            label = "Medicines",
            value = medicineCount,
            icon = Icons.Outlined.Medication,
            gradientColors = GradientTealCyan,
            delayMs = 0,
            visible = visible,
            onClick = onNavigateToMedicines,
            modifier = Modifier.weight(1f)
        )
        QuickStatMiniCard(
            label = "Reminders",
            value = reminderCount,
            icon = Icons.Outlined.Notifications,
            gradientColors = GradientTealCyan,
            delayMs = 200,
            visible = visible,
            onClick = onNavigateToReminders,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MedicationInventoryCard(
    lowStockCount: Int,
    totalTracked: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = ZariyaSurfaceElevated,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (lowStockCount > 0) GradientAmberRed[0].copy(alpha = 0.2f) else GradientTealCyan[0].copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = if (lowStockCount > 0) ZariyaError else ZariyaPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Medication Inventory",
                    color = ZariyaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (totalTracked == 0) {
                    Text(
                        text = "Track your medicine stock",
                        color = ZariyaTextSecondary,
                        fontSize = 14.sp
                    )
                } else if (lowStockCount > 0) {
                    Text(
                        text = "$lowStockCount medicines need refill soon",
                        color = ZariyaError,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Well stocked",
                        color = ZariyaSuccess,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatMiniCard(
    label: String,
    value: Int,
    icon: ImageVector,
    gradientColors: List<Color>,
    delayMs: Int,
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Staggered entrance
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs.toLong())
            cardVisible = true
        }
    }

    // Animated counter
    var displayValue by remember { mutableIntStateOf(0) }
    LaunchedEffect(value, cardVisible) {
        if (cardVisible) {
            for (i in 0..value) {
                displayValue = i
                delay(40L)
            }
        }
    }

    val offsetY by animateFloatAsState(
        targetValue = if (cardVisible) 0f else 40f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stat_offset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "stat_alpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY
                this.alpha = alpha
            }
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGlass)
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColors[0].copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
                // Border
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColors[0].copy(alpha = 0.2f),
                            gradientColors[0].copy(alpha = 0.05f)
                        )
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(14.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = gradientColors[0].copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$displayValue",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = gradientColors[0],
                letterSpacing = (-0.5).sp
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = ZariyaTextSecondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }
    }
}

// ─── Section 4: Quick Actions Grid ──────────────────────────────────────────────
@Composable
private fun QuickActionsGrid(
    onNavigateToReminders: () -> Unit,
    onNavigateToMedicines: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToInteractions: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section header
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ZariyaTextPrimary,
            letterSpacing = (-0.3).sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionItem(
                label = "Set Reminder",
                icon = Icons.Outlined.Notifications,
                gradientColors = GradientTealCyan,
                onClick = onNavigateToReminders,
                modifier = Modifier.weight(1f)
            )
            QuickActionItem(
                label = "Add Med",
                icon = Icons.Outlined.Medication,
                gradientColors = GradientAmberRed,
                onClick = onNavigateToMedicines,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionItem(
                label = "Scan Rx",
                icon = Icons.Outlined.CameraAlt,
                gradientColors = GradientPurple,
                onClick = onNavigateToScanner,
                modifier = Modifier.weight(1f)
            )
            QuickActionItem(
                label = "Interactions",
                icon = Icons.Outlined.Shield,
                gradientColors = GradientPink,
                onClick = onNavigateToInteractions,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionItem(
                label = "Health Data",
                icon = Icons.Outlined.FavoriteBorder,
                gradientColors = GradientBlue,
                onClick = onNavigateToHealth,
                modifier = Modifier.weight(1f)
            )
            QuickActionItem(
                label = "Analytics",
                icon = Icons.Outlined.Analytics,
                gradientColors = GradientCelebration,
                onClick = onNavigateToAnalytics,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "action_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGlass)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColors[0].copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColors[0].copy(alpha = 0.15f),
                            gradientColors[0].copy(alpha = 0.04f)
                        )
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(18.dp)
    ) {
        Column {
            // Gradient icon circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = gradientColors.map { it.copy(alpha = 0.15f) }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = gradientColors[0],
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ZariyaTextPrimary
            )
        }
    }
}

// ─── Section 5: Today's Activity Timeline ───────────────────────────────────────
@Composable
private fun TodayActivityTimeline(
    logs: List<com.project.zariya.feature.reminder.domain.model.DoseLog>,
    onLogClick: (com.project.zariya.feature.reminder.domain.model.DoseLog) -> Unit
) {
    Column {
        // Section header with gradient underline
        Column {
            Text(
                text = "Today's Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ZariyaTextPrimary,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Brush.horizontalGradient(GradientTealCyan))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayLogs = logs.take(6)
        displayLogs.forEachIndexed { index, log ->
            TimelineLogItem(
                log = log,
                isLast = index == displayLogs.lastIndex,
                index = index,
                onClick = { onLogClick(log) }
            )
        }
    }
}

@Composable
private fun TimelineLogItem(
    log: com.project.zariya.feature.reminder.domain.model.DoseLog,
    isLast: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    val statusColor = when (log.status) {
        DoseStatus.TAKEN -> ZariyaSuccess
        DoseStatus.MISSED -> ZariyaError
        DoseStatus.SKIPPED -> ZariyaWarning
        DoseStatus.SNOOZED -> ZariyaInfo
        DoseStatus.PENDING -> ZariyaTextTertiary
    }

    val statusContainerColor = when (log.status) {
        DoseStatus.TAKEN -> ZariyaSuccessContainer
        DoseStatus.MISSED -> ZariyaErrorContainer
        DoseStatus.SKIPPED -> ZariyaWarningContainer
        DoseStatus.SNOOZED -> ZariyaSurfaceElevated
        DoseStatus.PENDING -> ZariyaSurfaceElevated
    }

    // Animated entry
    var itemVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 80L)
        itemVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (itemVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "timeline_alpha_$index"
    )
    val offsetX by animateFloatAsState(
        targetValue = if (itemVisible) 0f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "timeline_offset_$index"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                translationX = offsetX
            }
    ) {
        // Timeline column (dot + line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Dot with glow
            Box(
                modifier = Modifier.size(14.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f))
                )
                // Dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            // Connecting line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(52.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = 0.3f),
                                    ZariyaSurfaceElevated.copy(alpha = 0.2f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content card
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceGlass)
                .clickable(onClick = onClick)
                .drawBehind {
                    drawRoundRect(
                        color = statusColor.copy(alpha = 0.08f),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(width = 0.5.dp.toPx())
                    )
                }
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.medicineName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ZariyaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = DateTimeUtils.run { log.scheduledTime.formatTime() },
                        fontSize = 12.sp,
                        color = ZariyaTextTertiary
                    )
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusContainerColor)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = log.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
