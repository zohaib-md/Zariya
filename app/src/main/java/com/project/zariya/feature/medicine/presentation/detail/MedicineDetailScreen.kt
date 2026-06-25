package com.project.zariya.feature.medicine.presentation.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.LoadingState
import com.project.zariya.core.ui.components.MedicineAvatar
import com.project.zariya.core.ui.theme.*
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.model.MedicineForm
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    medicineId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToAddReminder: () -> Unit = {},
    viewModel: MedicineDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(medicineId) {
        viewModel.loadMedicine(medicineId)
    }

    Scaffold(
        containerColor = ZariyaBackground,
        topBar = {
            TopAppBar(
                title = { Text("") }, // Clean top bar
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ZariyaSurfaceElevated)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZariyaTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ZariyaSurfaceElevated)
                            .clickable { showDeleteConfirm = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = ZariyaError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }
                uiState.error != null && uiState.medicine == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiState.error ?: "Something went wrong", color = ZariyaTextSecondary, fontSize = 15.sp)
                    }
                }
                uiState.medicine != null -> {
                    MedicineDetailContent(
                        uiState = uiState,
                        onEditClick = { onNavigateToEdit(medicineId) },
                        onAddReminderClick = onNavigateToAddReminder,
                        onAddStock = { amount -> viewModel.addStock(amount) },
                        onMarkTaken = { viewModel.markAsTaken() }
                    )
                }
            }
        }
    }

    if (showDeleteConfirm && uiState.medicine != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Medicine", color = ZariyaTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${uiState.medicine!!.name}? This action cannot be undone.", color = ZariyaTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMedicine()
                    showDeleteConfirm = false
                    onNavigateBack()
                }) {
                    Text("Delete", color = ZariyaError, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = ZariyaTextSecondary) }
            },
            containerColor = ZariyaSurface,
            titleContentColor = ZariyaTextPrimary,
            textContentColor = ZariyaTextSecondary,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineDetailContent(
    uiState: MedicineDetailUiState,
    onEditClick: () -> Unit,
    onAddReminderClick: () -> Unit,
    onAddStock: (Int) -> Unit,
    onMarkTaken: () -> Unit
) {
    var showAddStockSheet by remember { mutableStateOf(false) }
    val medicine = uiState.medicine!!
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            HeroSection(medicine)
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-20).dp), // Slight overlap with hero
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScheduleSection(uiState, onAddReminderClick)
                AdherenceSection(uiState)
                InventorySection(medicine, uiState.estimatedDaysRemaining) { showAddStockSheet = true }
                HistorySection(uiState.recentDoseLogs)
                ExtraInfoSection(medicine)
                Spacer(modifier = Modifier.height(120.dp)) // padding for bottom bar
            }
        }
        
        // Quick Actions Bottom Bar (Floating)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, ZariyaBackground.copy(alpha = 0.8f), ZariyaBackground)
                    )
                )
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Swipe to Confirm Button
                AestheticSwipeButton(
                    text = "Swipe to mark taken",
                    onSwipeComplete = onMarkTaken,
                    modifier = Modifier.weight(1f)
                )

                // Edit Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ZariyaPrimary.copy(alpha = 0.1f))
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ZariyaPrimary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }

    if (showAddStockSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddStockSheet = false },
            containerColor = ZariyaSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = ZariyaTextTertiary) }
        ) {
            AddStockSheet(medicine, onAddStock, onDismiss = { showAddStockSheet = false })
        }
    }
}

// ── Hero Section ────────────────────────────────────────────────────────
@Composable
private fun HeroSection(medicine: Medicine) {
    val formColor = when (medicine.form.name.uppercase()) {
        "TABLET" -> TabletColor
        "CAPSULE" -> CapsuleColor
        "SYRUP" -> SyrupColor
        "INJECTION" -> InjectionColor
        "DROPS" -> DropsColor
        "INHALER" -> InhalerColor
        "CREAM" -> CreamColor
        "PATCH" -> PatchColor
        else -> ZariyaPrimary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 48.dp), // Extra bottom padding for overlap
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(ZariyaSurfaceElevated)
                .drawBehind {
                    drawCircle(
                        color = formColor.copy(alpha = 0.15f),
                        radius = size.width / 2f
                    )
                }
                .border(2.dp, formColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            MedicineAvatar(form = medicine.form.name, size = 64.dp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = medicine.name,
            color = ZariyaTextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        if (medicine.genericName.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = medicine.genericName,
                color = ZariyaTextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            InfoChip(text = "${medicine.dosage} ${medicine.dosageUnit}")
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ZariyaTextTertiary))
            Spacer(Modifier.width(8.dp))
            InfoChip(text = medicine.category.displayName)
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ZariyaTextTertiary))
            Spacer(Modifier.width(8.dp))
            val statusColor = if (medicine.isActive) ZariyaSuccess else ZariyaTextTertiary
            val statusText = if (medicine.isActive) "Active" else "Paused"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                Spacer(Modifier.width(6.dp))
                Text(statusText, color = ZariyaTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Text(
        text = text,
        color = ZariyaTextSecondary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ZariyaSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

// ── Schedule Section ────────────────────────────────────────────────────
@Composable
private fun ScheduleSection(uiState: MedicineDetailUiState, onEditClick: () -> Unit) {
    PremiumCard(title = "Schedule", icon = Icons.Outlined.Schedule) {
        if (!uiState.medicine!!.isActive) {
            Text("Reminders are currently paused.", color = ZariyaTextSecondary, fontSize = 15.sp)
        } else if (uiState.upcomingDoses.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("No upcoming doses scheduled for today.", color = ZariyaTextSecondary, fontSize = 15.sp)
                
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ZariyaSurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ZariyaPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Reminder", color = ZariyaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                uiState.upcomingDoses.forEachIndexed { index, time ->
                    val isNext = index == 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isNext) ZariyaPrimary.copy(alpha = 0.1f) else ZariyaSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccessTime, 
                                contentDescription = null, 
                                tint = if (isNext) ZariyaPrimary else ZariyaTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(time)),
                                color = ZariyaTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (isNext) "Next dose" else "Upcoming today",
                                color = if (isNext) ZariyaPrimary else ZariyaTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Adherence Section ───────────────────────────────────────────────────
@Composable
private fun AdherenceSection(uiState: MedicineDetailUiState) {
    if (uiState.adherenceWeekly < 0 && uiState.adherenceMonthly < 0) return

    PremiumCard(title = "Adherence", icon = Icons.Outlined.TrendingUp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AdherenceRing("Weekly", uiState.adherenceWeekly)
            AdherenceRing("Monthly", uiState.adherenceMonthly)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        val recentAdherence = uiState.adherenceWeekly.takeIf { it >= 0 } ?: uiState.adherenceMonthly
        val (message, bgColor, iconColor) = when {
            recentAdherence >= 80 -> Triple("You're doing great! Keep up the consistency.", ZariyaSuccess.copy(alpha = 0.1f), ZariyaSuccess)
            recentAdherence >= 50 -> Triple("You missed a few doses. Let's try to stay on track.", ZariyaWarning.copy(alpha = 0.1f), ZariyaWarning)
            else -> Triple("Your adherence is low. Consider adjusting your reminders.", ZariyaError.copy(alpha = 0.1f), ZariyaError)
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = ZariyaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AdherenceRing(label: String, percentage: Float) {
    val displayPercentage = if (percentage < 0) 0f else percentage
    val ringColor = when {
        displayPercentage >= 75 -> ZariyaSuccess
        displayPercentage >= 50 -> ZariyaWarning
        else -> ZariyaError
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = displayPercentage / 100f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f }, color = ZariyaSurfaceElevated, strokeWidth = 8.dp, modifier = Modifier.fillMaxSize(), strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = { animatedProgress }, color = ringColor, strokeWidth = 8.dp, modifier = Modifier.fillMaxSize(), strokeCap = StrokeCap.Round
            )
            Text(
                text = if (percentage < 0) "--%" else "${displayPercentage.toInt()}%",
                color = ZariyaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(label, color = ZariyaTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Inventory Section ───────────────────────────────────────────────────
@Composable
private fun InventorySection(medicine: Medicine, daysRemaining: Int, onAddStockClick: () -> Unit) {
    if (!medicine.isStockTracked) return

    PremiumCard(title = "Inventory", icon = Icons.Outlined.Inventory2) {
        val isLiquid = medicine.form == MedicineForm.SYRUP || medicine.form == MedicineForm.DROPS || medicine.form == MedicineForm.INJECTION
        val currentStock = if (isLiquid) medicine.totalVolume ?: 0 else medicine.stockCount
        val ringColor = when {
            currentStock >= 20 -> ZariyaSuccess
            currentStock in 6..19 -> ZariyaWarning
            else -> ZariyaError
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { 1f }, color = ZariyaSurfaceElevated, strokeWidth = 6.dp, modifier = Modifier.fillMaxSize())
                CircularProgressIndicator(progress = { (currentStock / 50f).coerceIn(0f, 1f) }, color = ringColor, strokeWidth = 6.dp, modifier = Modifier.fillMaxSize(), strokeCap = StrokeCap.Round)
                Text(text = "$currentStock", color = ZariyaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                val unitString = when (medicine.form) {
                    MedicineForm.SYRUP, MedicineForm.DROPS -> "ml"
                    MedicineForm.OTHER -> "units"
                    else -> "${medicine.form.displayName}s"
                }
                Text(
                    text = if (currentStock == 0) "Out of Stock" else "$currentStock $unitString remaining",
                    color = ZariyaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (daysRemaining > 0) {
                    Text("~ $daysRemaining days left", color = ZariyaTextSecondary, fontSize = 14.sp)
                } else if (daysRemaining == 0 && currentStock > 0) {
                    Text("Refill immediately", color = ZariyaError, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ZariyaPrimary.copy(alpha = 0.1f))
                    .clickable { onAddStockClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Stock", tint = ZariyaPrimary, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ── History Section ─────────────────────────────────────────────────────
@Composable
private fun HistorySection(logs: List<DoseLog>) {
    if (logs.isEmpty()) return

    PremiumCard(title = "Recent History", icon = Icons.Outlined.History) {
        Column {
            logs.forEachIndexed { index, log ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Timeline
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
                        val iconColor = when (log.status) {
                            DoseStatus.TAKEN -> ZariyaSuccess
                            DoseStatus.MISSED -> ZariyaError
                            DoseStatus.SKIPPED -> ZariyaWarning
                            else -> ZariyaTextTertiary
                        }
                        Box(modifier = Modifier.padding(top = 4.dp).size(12.dp).clip(CircleShape).background(iconColor))
                        if (index != logs.size - 1) {
                            Box(modifier = Modifier.width(2.dp).height(48.dp).background(ZariyaSurfaceElevated))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.padding(bottom = if (index != logs.size - 1) 20.dp else 0.dp)) {
                        Text(
                            text = log.status.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = ZariyaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(log.actionTime ?: log.scheduledTime)),
                            color = ZariyaTextSecondary, fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Extra Info Section ──────────────────────────────────────────────────
@Composable
private fun ExtraInfoSection(medicine: Medicine) {
    if (medicine.doctorName.isNotBlank() || medicine.prescriptionNotes.isNotBlank() || medicine.notes.isNotBlank()) {
        PremiumCard(title = "Additional Info", icon = Icons.Outlined.Info) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (medicine.doctorName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(ZariyaPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ZariyaPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(medicine.doctorName, color = ZariyaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            if (medicine.doctorPhone.isNotBlank()) {
                                val uriHandler = LocalUriHandler.current
                                Row(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { uriHandler.openUri("tel:${medicine.doctorPhone}") }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = ZariyaPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(medicine.doctorPhone, color = ZariyaPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                if (medicine.prescriptionNotes.isNotBlank()) {
                    Column {
                        Text("Prescription Notes", color = ZariyaTextTertiary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(medicine.prescriptionNotes, color = ZariyaTextSecondary, fontSize = 15.sp, lineHeight = 22.sp)
                    }
                }
                if (medicine.notes.isNotBlank()) {
                    Column {
                        Text("Personal Notes", color = ZariyaTextTertiary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(medicine.notes, color = ZariyaTextSecondary, fontSize = 15.sp, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}

// ── Shared UI Components ────────────────────────────────────────────────
@Composable
private fun PremiumCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ZariyaSurface)
            .border(1.dp, ZariyaSurfaceElevated, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(ZariyaPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = ZariyaPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = ZariyaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun AddStockSheet(medicine: Medicine, onAddStock: (Int) -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(ZariyaSurfaceElevated))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Add Stock", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ZariyaTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Quickly restock ${medicine.name}", fontSize = 15.sp, color = ZariyaTextSecondary)
        Spacer(modifier = Modifier.height(32.dp))
        
        val isLiquid = medicine.form == MedicineForm.SYRUP || medicine.form == MedicineForm.DROPS || medicine.form == MedicineForm.INJECTION
        val amounts = if (isLiquid) listOf(50, 100, 200) else listOf(10, 20, 30)
        val addUnitString = if (isLiquid) "ml" else if (medicine.form == MedicineForm.OTHER) "units" else "${medicine.form.displayName}s"

        amounts.forEach { amount ->
            Button(
                onClick = { onAddStock(amount); onDismiss() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZariyaSurfaceElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+$amount $addUnitString", color = ZariyaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        val customAmount = remember { mutableStateOf("") }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = customAmount.value, onValueChange = { customAmount.value = it },
                label = { Text("Custom Amount") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.weight(1f), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ZariyaPrimary, unfocusedBorderColor = ZariyaSurfaceElevated)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { customAmount.value.toIntOrNull()?.let { amount -> onAddStock(amount); onDismiss() } },
                modifier = Modifier.height(56.dp).padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZariyaPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Custom Aesthetic Swipe Button ───────────────────────────────────────
@Composable
fun AestheticSwipeButton(
    text: String,
    onSwipeComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val width = remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val thumbSize = 56.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val dragOffset = remember { Animatable(0f) }
    var isCompleted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    // Premium track gradient (Dark translucent glass effect)
    val trackGradient = Brush.horizontalGradient(
        colors = listOf(
            ZariyaPrimary.copy(alpha = 0.1f),
            ZariyaPrimary.copy(alpha = 0.05f)
        )
    )
    
    // Vibrant thumb gradient
    val thumbGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00E676), // Vibrant Neon Green
            Color(0xFF1DE9B6)  // Vibrant Cyan
        )
    )

    Box(
        modifier = modifier
            .height(thumbSize)
            .clip(CircleShape)
            .background(trackGradient)
            .border(1.dp, ZariyaPrimary.copy(alpha = 0.15f), CircleShape)
            .onSizeChanged { width.intValue = it.width },
        contentAlignment = Alignment.CenterStart
    ) {
        // Shimmering or subtle text
        val alphaByOffset = if (width.intValue > 0) {
            (1f - (dragOffset.value / (width.intValue - thumbSizePx))).coerceIn(0f, 1f)
        } else 1f

        Text(
            text = if (isCompleted) "Taken!" else text,
            color = if (isCompleted) ZariyaSuccess else ZariyaTextSecondary.copy(alpha = 0.8f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp) // Offset for the thumb
                .alpha(if (isCompleted) 1f else alphaByOffset),
            textAlign = TextAlign.Center
        )

        // Draggable Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                .size(thumbSize)
                .padding(4.dp) // Padding to make it slightly smaller than the track
                .clip(CircleShape)
                .then(if (isCompleted) Modifier.background(ZariyaSuccess) else Modifier.background(thumbGradient))
                .pointerInput(isCompleted) {
                    if (isCompleted) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val maxOffset = width.intValue - thumbSizePx
                            if (dragOffset.value > maxOffset * 0.7f) {
                                coroutineScope.launch {
                                    dragOffset.animateTo(maxOffset, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                    isCompleted = true
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onSwipeComplete()
                                }
                            } else {
                                coroutineScope.launch {
                                    dragOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val maxOffset = width.intValue - thumbSizePx
                                val newOffset = (dragOffset.value + dragAmount).coerceIn(0f, maxOffset)
                                dragOffset.snapTo(newOffset)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
