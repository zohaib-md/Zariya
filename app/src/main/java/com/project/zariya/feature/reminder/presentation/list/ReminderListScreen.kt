package com.project.zariya.feature.reminder.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.EmptyState
import com.project.zariya.core.ui.components.LoadingState
import com.project.zariya.core.ui.components.MedicineAvatar
import com.project.zariya.core.ui.icons.ZariyaIcons
import com.project.zariya.core.ui.theme.*
import com.project.zariya.core.util.DateTimeUtils
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.model.ScheduleType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    onNavigateToAddReminder: () -> Unit = {},
    onNavigateToEditReminder: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: ReminderListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gradientBrush = ZariyaGradientHorizontal

    Scaffold(
        containerColor = ZariyaBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Reminders",
                            color = ZariyaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ZariyaBackground)
                )
                // Gradient divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    ZariyaPrimary.copy(alpha = 0.3f),
                                    ZariyaGradientEnd.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .drawBehind {
                        drawRoundRect(
                            brush = gradientBrush,
                            cornerRadius = CornerRadius(30.dp.toPx()),
                            alpha = 0.3f
                        )
                    }
                    .clip(CircleShape)
                    .background(gradientBrush)
                    .clickable(onClick = onNavigateToAddReminder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Reminder",
                    tint = ZariyaTextOnPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            uiState.reminders.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.Schedule,
                    title = "No Reminders Set",
                    subtitle = "Set up medication reminders to never miss a dose",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        uiState.reminders,
                        key = { _, r -> r.id }
                    ) { index, reminder ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 70L)
                            itemVisible = true
                        }

                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = fadeIn(tween(300)) + slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialOffsetY = { it / 2 }
                            )
                        ) {
                            PremiumReminderCard(
                                reminder = reminder,
                                onEditClick = { onNavigateToEditReminder(reminder.id) },
                                onToggleClick = { isActive ->
                                    viewModel.toggleReminder(reminder, isActive)
                                },
                                onDeleteClick = {
                                    viewModel.deleteReminder(reminder.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumReminderCard(
    reminder: Reminder,
    onEditClick: () -> Unit,
    onToggleClick: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val gradientBorder = Brush.horizontalGradient(
        listOf(ZariyaPrimary.copy(alpha = 0.2f), ZariyaGradientEnd.copy(alpha = 0.15f))
    )

    Box(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, gradientBorder, RoundedCornerShape(20.dp))
            .background(ZariyaSurface.copy(alpha = 0.85f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onEditClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Medicine avatar
                MedicineAvatar(
                    form = reminder.medicineForm,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.medicineName,
                        color = ZariyaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = reminder.dosage,
                        color = ZariyaTextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Active toggle with gradient teal
                Switch(
                    checked = reminder.isActive,
                    onCheckedChange = onToggleClick,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ZariyaPrimary,
                        checkedTrackColor = ZariyaPrimary.copy(alpha = 0.3f),
                        uncheckedThumbColor = ZariyaTextTertiary,
                        uncheckedTrackColor = ZariyaSurfaceElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Schedule info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = ZariyaPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildScheduleText(reminder),
                    color = ZariyaTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Reminder",
                        tint = ZariyaError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Next trigger countdown
            if (reminder.isActive && reminder.nextTriggerTime > System.currentTimeMillis()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ZariyaPrimary.copy(alpha = 0.08f),
                                    ZariyaGradientEnd.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            0.5.dp,
                            ZariyaPrimary.copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = ZariyaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Next in ${DateTimeUtils.formatCountdown(reminder.nextTriggerTime)}",
                            color = ZariyaPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun buildScheduleText(reminder: Reminder): String {
    val timeStr = reminder.scheduledTimes.joinToString(", ")
    return when (reminder.scheduleType) {
        ScheduleType.DAILY -> "Daily at $timeStr"
        ScheduleType.EVERY_N_HOURS -> "Every ${reminder.intervalHours}h"
        ScheduleType.SPECIFIC_DAYS -> {
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val days = reminder.selectedDays.mapNotNull { dayNames.getOrNull(it - 1) }.joinToString(", ")
            "$days at $timeStr"
        }
        ScheduleType.CYCLIC -> "${reminder.cycleDaysOn} on / ${reminder.cycleDaysOff} off"
    }
}
