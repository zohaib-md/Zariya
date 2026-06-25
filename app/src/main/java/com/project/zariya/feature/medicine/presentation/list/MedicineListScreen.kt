package com.project.zariya.feature.medicine.presentation.list

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.project.zariya.core.util.DateTimeUtils.formatTime
import com.project.zariya.core.ui.components.EmptyState
import com.project.zariya.core.ui.components.LoadingState
import com.project.zariya.core.ui.components.MedicineAvatar
import com.project.zariya.core.ui.components.ZariyaCard
import com.project.zariya.core.ui.icons.ZariyaIcons
import com.project.zariya.core.ui.theme.*
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.model.MedicineCategory
import kotlinx.coroutines.delay
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlin.math.abs
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    onNavigateToAddMedicine: () -> Unit = {},
    onNavigateToMedicineDetail: (String) -> Unit = {},
    viewModel: MedicineListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val gradientBrush = ZariyaGradientHorizontal

    Scaffold(
        containerColor = ZariyaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Medicines",
                        color = ZariyaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZariyaBackground),
                actions = {
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ZariyaPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Gradient FAB with glow
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
                    .clickable(onClick = onNavigateToAddMedicine),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Medicine",
                    tint = ZariyaTextOnPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Animated Search Bar
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = fadeIn() + slideInVertically { -it }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            brush = gradientBrush.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Search medicines...", color = ZariyaTextTertiary, fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = ZariyaPrimary)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = ZariyaSurfaceElevated,
                            unfocusedContainerColor = ZariyaSurfaceElevated,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = ZariyaPrimary,
                            focusedTextColor = ZariyaTextPrimary,
                            unfocusedTextColor = ZariyaTextPrimary
                        ),
                        singleLine = true
                    )
                }
            }

            // Category Filter Chips
            PremiumCategoryFilterRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )

            when {
                uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
                uiState.medicines.isEmpty() -> {
                    EmptyState(
                        icon = ZariyaIcons.PillBottle,
                        title = "No Medicines Found",
                        subtitle = if (uiState.searchQuery.isNotEmpty() || uiState.selectedCategory != null)
                            "Try adjusting your filters"
                        else "Tap + to add your first medicine",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            uiState.medicines,
                            key = { _, em -> em.medicine.id }
                        ) { index, enrichedMedicine ->
                            var itemVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 60L)
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
                                PremiumMedicineCard(
                                    enrichedMedicine = enrichedMedicine,
                                    onClick = { onNavigateToMedicineDetail(enrichedMedicine.medicine.id) }
                                )
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Brush.copy(alpha: Float): Brush {
    return Brush.horizontalGradient(
        listOf(ZariyaPrimary.copy(alpha = alpha), ZariyaGradientEnd.copy(alpha = alpha))
    )
}

@Composable
private fun PremiumCategoryFilterRow(
    selectedCategory: MedicineCategory?,
    onCategorySelected: (MedicineCategory?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PremiumFilterChip("All", selectedCategory == null) { onCategorySelected(null) }
        }
        items(MedicineCategory.entries.toTypedArray()) { category ->
            PremiumFilterChip(category.displayName, selectedCategory == category) { onCategorySelected(category) }
        }
    }
}

@Composable
private fun PremiumFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val gradientBrush = ZariyaGradientHorizontal
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (isSelected) {
                    Modifier.background(gradientBrush)
                } else {
                    Modifier
                        .background(ZariyaSurfaceElevated)
                        .border(1.dp, ZariyaPrimary.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) ZariyaTextOnPrimary else ZariyaTextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PremiumMedicineCard(enrichedMedicine: EnrichedMedicine, onClick: () -> Unit) {
    val medicine = enrichedMedicine.medicine
    val isCompleted = enrichedMedicine.isCompletedToday
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val gradientBorder = Brush.horizontalGradient(
        listOf(
            if (isCompleted) ZariyaSuccess.copy(alpha = 0.5f) else ZariyaPrimary.copy(alpha = 0.2f),
            if (isCompleted) ZariyaSuccess.copy(alpha = 0.1f) else ZariyaGradientEnd.copy(alpha = 0.15f)
        )
    )

    // Form color for left accent
    val formColor = if (isCompleted) ZariyaSuccess else when (medicine.form.name.uppercase()) {
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

    Box(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isCompleted) ZariyaSuccess.copy(alpha = 0.03f) else ZariyaSurface)
            .drawBehind {
                drawRect(
                    color = formColor,
                    size = androidx.compose.ui.geometry.Size(6.dp.toPx(), size.height)
                )
            }
            .border(1.dp, gradientBorder, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, top = 20.dp, end = 16.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MedicineAvatar(
                    form = medicine.form.name,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = medicine.name,
                            color = ZariyaTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Category badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ZariyaPrimary.copy(alpha = 0.08f))
                                .border(0.5.dp, ZariyaPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = medicine.category.displayName,
                                color = ZariyaPrimaryDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (medicine.genericName.isNotBlank()) {
                        Text(
                            text = medicine.genericName,
                            color = ZariyaTextTertiary,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${medicine.dosage} ${medicine.dosageUnit} · ${medicine.form.displayName}",
                        color = ZariyaTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isCompleted) {
                    InfoChip(
                        icon = Icons.Default.Check,
                        text = "Completed Today",
                        tint = ZariyaSuccess,
                        modifier = Modifier.weight(1f),
                        backgroundColor = ZariyaSuccess.copy(alpha = 0.1f)
                    )
                } else {
                    // Next Dose Chip
                    val nextDoseText = enrichedMedicine.nextDoseTime?.formatTime() ?: "Not set"
                    InfoChip(
                        icon = Icons.Default.Schedule,
                        text = nextDoseText,
                        tint = ZariyaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Adherence Chip
                val adherenceColor = when {
                    enrichedMedicine.adherencePercentage < 0 -> ZariyaTextTertiary
                    enrichedMedicine.adherencePercentage >= 75 -> ZariyaSuccess
                    enrichedMedicine.adherencePercentage >= 50 -> ZariyaWarning
                    else -> ZariyaError
                }
                val adherenceText = if (enrichedMedicine.adherencePercentage < 0) "--" else "${enrichedMedicine.adherencePercentage}%"
                InfoChip(
                    icon = Icons.Default.TrendingUp,
                    text = adherenceText,
                    tint = adherenceColor,
                    modifier = Modifier.weight(1f)
                )

                // Stock Chip
                val (stockColor, _) = when (enrichedMedicine.stockStatus) {
                    StockStatus.HEALTHY -> ZariyaSuccess to "Healthy"
                    StockStatus.LOW -> ZariyaWarning to "Low"
                    StockStatus.CRITICAL -> ZariyaError to "Critical"
                    StockStatus.NOT_TRACKED -> ZariyaTextTertiary to "Not tracked"
                }
                val stockText = if (medicine.isStockTracked) "${medicine.currentStock} ${medicine.currentStockUnit} left" else "No track"
                InfoChip(
                    icon = Icons.Default.Inventory,
                    text = stockText,
                    tint = if (isCompleted) ZariyaSuccess else stockColor,
                    modifier = Modifier.weight(1.2f)
                )
            }


        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ZariyaSurfaceElevated
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = ZariyaTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

/**
 * Swipe card states:
 * - IDLE: offset = 0
 * - REVEALING_ACTIONS: offset = -actionMenuWidthPx (left swipe, shows action buttons)
 * - CONFIRMED_TAKEN: card stays at a confirmation stop position (right swipe threshold crossed)
 *
 * The card NEVER flies off screen. On right swipe past threshold, we:
 *   1. Animate to a small confirmation offset
 *   2. Fire onMarkTaken() exactly once
 *   3. Wait briefly, then snap back to 0
 *
 * Action buttons are rendered in a separate composable layer that is NOT inside the
 * pointerInput modifier, so taps always reach them.
 */
@Composable
fun PremiumSwipeableCard(
    isCompleted: Boolean,
    onMarkTaken: () -> Unit,
    onAddStock: () -> Unit,
    onEdit: () -> Unit,
    onUndo: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val actionMenuWidthPx = with(density) { 140.dp.toPx() }
    val confirmationStopPx = with(density) { 80.dp.toPx() } // right swipe stops here visually
    val swipeThresholdPx = with(density) { 120.dp.toPx() } // must drag this far to trigger

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    // Prevent double-firing markAsTaken
    var hasFiredTaken by remember { mutableStateOf(false) }

    // Track if the action menu is currently showing (card swiped left and snapped)
    val isMenuRevealed = offsetX.value < -(actionMenuWidthPx * 0.5f)

    // Reset hasFiredTaken flag when isCompleted changes externally
    LaunchedEffect(isCompleted) {
        hasFiredTaken = false
        // If the card was swiped right and confirmed, snap back smoothly
        if (offsetX.value != 0f) {
            delay(300)
            offsetX.animateTo(
                0f,
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // ── LAYER 1: Background action buttons ──
        // Rendered at full size behind the card. Only visible when card is offset.
        // These are NOT inside the pointerInput so taps always work.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    when {
                        offsetX.value > 0 -> ZariyaSuccess
                        offsetX.value < 0 -> ZariyaPrimary
                        else -> Color.Transparent
                    }
                )
                .graphicsLayer(
                    alpha = (abs(offsetX.value) / 80f).coerceIn(0f, 1f)
                )
        ) {
            // Right swipe indicator (just visual, no clickable)
            if (offsetX.value > 10) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mark Taken",
                        tint = ZariyaSurface,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Mark Taken",
                        color = ZariyaSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Left swipe action buttons — these ARE clickable
            if (offsetX.value < -10) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isCompleted) {
                        // Undo button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(70.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onUndo()
                                    coroutineScope.launch {
                                        offsetX.animateTo(
                                            0f,
                                            spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    }
                                }
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = ZariyaSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Undo",
                                color = ZariyaSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        // Add Stock button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(64.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onAddStock()
                                    coroutineScope.launch {
                                        offsetX.animateTo(
                                            0f,
                                            spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    }
                                }
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.AddShoppingCart,
                                contentDescription = "Add Stock",
                                tint = ZariyaSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Stock",
                                color = ZariyaSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Edit button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(64.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onEdit()
                                    coroutineScope.launch {
                                        offsetX.animateTo(
                                            0f,
                                            spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    }
                                }
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = ZariyaSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Edit",
                                color = ZariyaSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // ── LAYER 2: Foreground card with drag gesture ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(isCompleted) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                when {
                                    // RIGHT SWIPE: past threshold and not yet completed → mark taken
                                    offsetX.value > swipeThresholdPx && !isCompleted && !hasFiredTaken -> {
                                        hasFiredTaken = true
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                        onMarkTaken()
                                        // Animate to a small visible confirmation position, then back
                                        offsetX.animateTo(
                                            confirmationStopPx,
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                        delay(500)
                                        offsetX.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }

                                    // LEFT SWIPE: past 40% of action menu width → snap to reveal
                                    offsetX.value < -(actionMenuWidthPx * 0.35f) -> {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        offsetX.animateTo(
                                            -actionMenuWidthPx,
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }

                                    // Didn't pass any threshold → snap back
                                    else -> {
                                        offsetX.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                var newOffset = offsetX.value + dragAmount

                                // Completed cards: block swipe right, only allow left for undo
                                if (isCompleted && newOffset > 0f) {
                                    newOffset = offsetX.value + (dragAmount * 0.15f)
                                }
                                // Block swipe right if already taken
                                if (!isCompleted && hasFiredTaken && newOffset > 0f) {
                                    newOffset = 0f
                                }
                                // Rubber-band resistance past action menu
                                if (newOffset < -actionMenuWidthPx * 1.3f) {
                                    newOffset = offsetX.value + (dragAmount * 0.1f)
                                }
                                // Gentle resistance on right swipe past confirmation stop
                                if (newOffset > swipeThresholdPx * 1.5f) {
                                    newOffset = offsetX.value + (dragAmount * 0.15f)
                                }

                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
