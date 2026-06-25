package com.project.zariya.feature.reminder.presentation.add_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.ZariyaPrimaryButton
import com.project.zariya.core.ui.theme.ZariyaBackground
import com.project.zariya.core.ui.theme.ZariyaError
import com.project.zariya.core.ui.theme.ZariyaPrimary
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaSurfaceElevated
import com.project.zariya.core.ui.theme.ZariyaTextOnPrimary
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import com.project.zariya.feature.reminder.domain.model.ScheduleType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val scrollState = rememberScrollState()
    var showTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf<Int?>(null) }
    var showMedicineDropdown by remember { mutableStateOf(false) }

    // We check if the saved state has an ID to determine if it's editing
    // Unfortunately, we don't have the reminderId directly in the screen, but we can check if selectedMedicineId is not empty for editing state maybe, or just keep it simple.
    val isEditMode = uiState.selectedMedicineId.isNotEmpty()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = ZariyaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Reminder" else "Add Reminder",
                        color = ZariyaTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZariyaPrimary
                        )
                    }
                },
                actions = {
                    androidx.compose.material3.Switch(
                        checked = uiState.isActive,
                        onCheckedChange = { viewModel.toggleActive() },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = ZariyaSurface,
                            checkedTrackColor = ZariyaPrimary,
                            uncheckedThumbColor = ZariyaTextSecondary,
                            uncheckedTrackColor = ZariyaSurfaceElevated
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZariyaBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Medicine Picker ──────────────────────────────────────
            SectionLabel("Select Medicine")

            Box {
                OutlinedTextField(
                    value = uiState.selectedMedicineName.ifEmpty { "Choose a medicine" },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMedicineDropdown = true },
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = ZariyaTextSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = ZariyaSurfaceElevated,
                        disabledTextColor = if (uiState.selectedMedicineName.isNotEmpty()) ZariyaTextPrimary else ZariyaTextSecondary,
                        disabledContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )

                DropdownMenu(
                    expanded = showMedicineDropdown,
                    onDismissRequest = { showMedicineDropdown = false },
                    modifier = Modifier.background(ZariyaSurfaceElevated)
                ) {
                    if (medicines.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "No medicines available",
                                    color = ZariyaTextSecondary
                                )
                            },
                            onClick = { showMedicineDropdown = false }
                        )
                    } else {
                        medicines.forEach { medicine ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = medicine.name,
                                            color = ZariyaTextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${medicine.dosage} ${medicine.dosageUnit}",
                                            color = ZariyaTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.onMedicineSelected(medicine)
                                    showMedicineDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Dosage ──────────────────────────────────────────────
            SectionLabel("Dosage")
            OutlinedTextField(
                value = uiState.dosage,
                onValueChange = viewModel::onDosageChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 500 mg", color = ZariyaTextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZariyaPrimary,
                    focusedLabelColor = ZariyaPrimary,
                    unfocusedBorderColor = ZariyaSurfaceElevated,
                    unfocusedTextColor = ZariyaTextPrimary,
                    focusedTextColor = ZariyaTextPrimary
                )
            )

            // ── Schedule Type ───────────────────────────────────────
            SectionLabel("Schedule Type")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScheduleType.entries.filter { it != ScheduleType.CYCLIC }.forEach { type ->
                    ScheduleTypeChip(
                        text = type.displayName,
                        isSelected = uiState.scheduleType == type,
                        onClick = { viewModel.onScheduleTypeChange(type) }
                    )
                }
            }

            // ── Scheduled Times ─────────────────────────────────────
            SectionLabel("Scheduled Times")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.scheduledTimes.forEachIndexed { index, time ->
                    TimeChipRow(
                        time = time,
                        canRemove = uiState.scheduledTimes.size > 1,
                        onRemove = { viewModel.removeTime(index) },
                        onClick = {
                            editingTimeIndex = index
                            showTimePicker = true
                        }
                    )
                }

                Surface(
                    color = ZariyaSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { 
                        editingTimeIndex = null
                        showTimePicker = true 
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add time",
                            tint = ZariyaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Time",
                            color = ZariyaPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ── Day Selector (for SPECIFIC_DAYS) ────────────────────
            if (uiState.scheduleType == ScheduleType.SPECIFIC_DAYS) {
                SectionLabel("Select Days")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    dayLabels.forEachIndexed { index, label ->
                        val dayNumber = index + 1 // 1=Mon, 7=Sun
                        val isSelected = dayNumber in uiState.selectedDays
                        DayChip(
                            label = label,
                            isSelected = isSelected,
                            onClick = { viewModel.toggleDay(dayNumber) }
                        )
                    }
                }
            }

            // ── Interval Input (for EVERY_N_HOURS) ──────────────────
            if (uiState.scheduleType == ScheduleType.EVERY_N_HOURS) {
                SectionLabel("Interval (hours)")
                OutlinedTextField(
                    value = uiState.intervalHours.toString(),
                    onValueChange = viewModel::onIntervalHoursChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 8", color = ZariyaTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZariyaPrimary,
                        focusedLabelColor = ZariyaPrimary,
                        unfocusedBorderColor = ZariyaSurfaceElevated,
                        unfocusedTextColor = ZariyaTextPrimary,
                        focusedTextColor = ZariyaTextPrimary
                    )
                )
            }

            // ── Error Message ───────────────────────────────────────
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = ZariyaError,
                    fontSize = 14.sp
                )
            }

            // ── Save Button ─────────────────────────────────────────
            ZariyaPrimaryButton(
                text = "Save Reminder",
                onClick = viewModel::onSave,
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ── Time Picker Dialog ──────────────────────────────────────────
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { 
                showTimePicker = false 
                editingTimeIndex = null
            },
            onTimeSelected = { hour, minute ->
                val amPm = if (hour >= 12) "PM" else "AM"
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                val formattedTime = String.format(
                    Locale.US,
                    "%02d:%02d %s",
                    displayHour,
                    minute,
                    amPm
                )
                
                val index = editingTimeIndex
                if (index != null) {
                    viewModel.updateTime(index, formattedTime)
                } else {
                    viewModel.addTime(formattedTime)
                }
                
                showTimePicker = false
                editingTimeIndex = null
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = ZariyaTextPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
}

@Composable
private fun ScheduleTypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) ZariyaPrimary else ZariyaSurfaceElevated,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (isSelected) ZariyaTextOnPrimary else ZariyaTextPrimary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TimeChipRow(
    time: String,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        color = ZariyaSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = ZariyaPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = time,
                color = ZariyaTextPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove time",
                        tint = ZariyaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) ZariyaPrimary else ZariyaSurfaceElevated)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) ZariyaTextOnPrimary else ZariyaTextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ZariyaSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    color = ZariyaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = ZariyaSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable(onClick = onDismiss)
                    ) {
                        Text(
                            text = "Cancel",
                            color = ZariyaTextSecondary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        color = ZariyaPrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable {
                            onTimeSelected(timePickerState.hour, timePickerState.minute)
                        }
                    ) {
                        Text(
                            text = "OK",
                            color = ZariyaTextOnPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
