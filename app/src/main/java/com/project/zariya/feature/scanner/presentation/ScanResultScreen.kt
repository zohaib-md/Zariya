package com.project.zariya.feature.scanner.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.EmptyState
import com.project.zariya.core.ui.components.ZariyaCard
import com.project.zariya.core.ui.components.ZariyaPrimaryButton
import com.project.zariya.core.ui.theme.ZariyaBackground
import com.project.zariya.core.ui.theme.ZariyaError
import com.project.zariya.core.ui.theme.ZariyaPrimary
import com.project.zariya.core.ui.theme.ZariyaSuccess
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaSurfaceElevated
import com.project.zariya.core.ui.theme.ZariyaSurfaceVariant
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import com.project.zariya.core.ui.theme.ZariyaWarning
import com.project.zariya.feature.scanner.domain.model.ExtractedMedicine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    onNavigateBack: () -> Unit,
    onAddSelectedMedicines: (List<ExtractedMedicine>) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRawTextExpanded by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onAddSelectedMedicines(uiState.selectedMedicines.toList())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Results",
                        color = ZariyaTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZariyaTextPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.extractedMedicines.isNotEmpty()) {
                        val allSelected = uiState.selectedMedicines.size == uiState.extractedMedicines.size
                        TextButton(
                            onClick = {
                                if (allSelected) {
                                    viewModel.deselectAllMedicines()
                                } else {
                                    viewModel.selectAllMedicines()
                                }
                            }
                        ) {
                            Text(
                                text = if (allSelected) "Deselect All" else "Select All",
                                color = ZariyaPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZariyaSurfaceElevated
                )
            )
        },
        bottomBar = {
            if (uiState.selectedMedicines.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZariyaSurfaceElevated)
                        .padding(16.dp)
                ) {
                    ZariyaPrimaryButton(
                        text = "Add ${uiState.selectedMedicines.size} Medicine${if (uiState.selectedMedicines.size > 1) "s" else ""}",
                        onClick = {
                            viewModel.saveSelectedMedicines()
                        },
                        isLoading = uiState.isProcessing,
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = ZariyaBackground
    ) { paddingValues ->
        if (uiState.rawText.isBlank() && uiState.extractedMedicines.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Description,
                title = "No Results",
                subtitle = "No text was detected. Try scanning again with better lighting.",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Raw OCR Text Section
                item {
                    RawTextCard(
                        rawText = uiState.rawText,
                        isExpanded = isRawTextExpanded,
                        onToggleExpand = { isRawTextExpanded = !isRawTextExpanded }
                    )
                }

                // Extracted Medicines Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Extracted Medicines",
                            color = ZariyaTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.extractedMedicines.size} found",
                            color = ZariyaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                if (uiState.extractedMedicines.isEmpty() && uiState.rawText.isNotBlank()) {
                    item {
                        ZariyaCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No medicines could be extracted",
                                    color = ZariyaTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "The scanned text didn't contain recognizable medicine information. Try scanning a clearer image.",
                                    color = ZariyaTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Medicine Cards
                items(
                    items = uiState.extractedMedicines,
                    key = { "${it.name}_${it.dosage}" }
                ) { medicine ->
                    ExtractedMedicineCard(
                        medicine = medicine,
                        isSelected = uiState.selectedMedicines.contains(medicine),
                        onToggleSelection = { viewModel.onMedicineSelected(medicine) }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun RawTextCard(
    rawText: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    ZariyaCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = ZariyaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Raw OCR Text",
                        color = ZariyaTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onToggleExpand) {
                    Text(
                        text = if (isExpanded) "Collapse" else "Expand",
                        color = ZariyaPrimary,
                        fontSize = 13.sp
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ZariyaSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ZariyaSurfaceVariant)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = rawText,
                        color = ZariyaTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rawText,
                    color = ZariyaTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ExtractedMedicineCard(
    medicine: ExtractedMedicine,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    ZariyaCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggleSelection
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ZariyaPrimary,
                        uncheckedColor = ZariyaTextSecondary,
                        checkmarkColor = com.project.zariya.core.ui.theme.ZariyaTextOnPrimary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        color = ZariyaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (medicine.dosage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = medicine.dosage,
                            color = ZariyaPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                ConfidenceBadge(confidence = medicine.confidence)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (medicine.frequency.isNotBlank()) {
                    MedicineInfoChip(
                        label = "Frequency",
                        value = medicine.frequency,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (medicine.duration.isNotBlank()) {
                    MedicineInfoChip(
                        label = "Duration",
                        value = medicine.duration,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence progress bar
            ConfidenceBar(confidence = medicine.confidence)
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float) {
    val percentage = (confidence * 100).toInt()
    val color = when {
        confidence >= 0.7f -> ZariyaSuccess
        confidence >= 0.4f -> ZariyaWarning
        else -> ZariyaError
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$percentage%",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ConfidenceBar(confidence: Float) {
    val color = when {
        confidence >= 0.7f -> ZariyaSuccess
        confidence >= 0.4f -> ZariyaWarning
        else -> ZariyaError
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Confidence",
                color = ZariyaTextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = "${(confidence * 100).toInt()}%",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { confidence },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = ZariyaSurfaceVariant,
        )
    }
}

@Composable
private fun MedicineInfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ZariyaSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = ZariyaTextSecondary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = ZariyaTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
