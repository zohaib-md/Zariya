package com.project.zariya.feature.medicine.presentation.add_edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.MedicineAvatar
import com.project.zariya.core.ui.components.ZariyaCard
import com.project.zariya.core.ui.components.ZariyaPrimaryButton
import com.project.zariya.core.ui.theme.*
import com.project.zariya.feature.medicine.domain.model.MedicineCategory
import com.project.zariya.feature.medicine.domain.model.MedicineForm
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditMedicineScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddReminder: (String) -> Unit,
    viewModel: AddEditMedicineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            if (uiState.setReminder && uiState.savedMedicineId != null) {
                onNavigateToAddReminder(uiState.savedMedicineId!!)
            } else {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        containerColor = ZariyaBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add Medicine", color = ZariyaTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZariyaPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZariyaBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = ZariyaBackground,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        ) {
                            Text("Back", color = ZariyaTextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(64.dp))
                    }

                    if (pagerState.currentPage < 4) {
                        ZariyaPrimaryButton(
                            text = "Next",
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier.width(120.dp)
                        )
                    } else {
                        ZariyaPrimaryButton(
                            text = "Save",
                            onClick = { viewModel.saveMedicine() },
                            modifier = Modifier.width(120.dp),
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Custom Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 5) {
                    val isCompleted = i <= pagerState.currentPage
                    val weight = if (isCompleted) 1.5f else 1f
                    val animatedWeight by animateFloatAsState(targetValue = weight, label = "weight")
                    Box(
                        modifier = Modifier
                            .weight(animatedWeight)
                            .height(6.dp)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isCompleted) ZariyaPrimary else ZariyaSurfaceElevated)
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) { page ->
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (page) {
                        0 -> StepMedicineInfo(uiState, viewModel)
                        1 -> StepDosage(uiState, viewModel)
                        2 -> StepSchedule(uiState, viewModel)
                        3 -> StepInventory(uiState, viewModel)
                        4 -> StepReview(uiState, viewModel)
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun StepMedicineInfo(uiState: AddEditMedicineUiState, viewModel: AddEditMedicineViewModel) {
    Text(
        text = "Medicine Information",
        color = ZariyaTextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
    
    Text(
        text = "Let's start with the basics.",
        color = ZariyaTextSecondary,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Form Selection
    Text(
        text = "Form",
        color = ZariyaTextPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(MedicineForm.entries.toTypedArray()) { form ->
            FormSelector(
                form = form,
                isSelected = uiState.form == form,
                onClick = { viewModel.updateState(uiState.copy(form = form)) }
            )
        }
    }

    // Category Selection
    Text(
        text = "Category",
        color = ZariyaTextPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(MedicineCategory.entries.toTypedArray()) { category ->
            Surface(
                color = if (uiState.category == category) ZariyaPrimary else ZariyaSurfaceElevated,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { viewModel.updateState(uiState.copy(category = category)) }
            ) {
                Text(
                    text = category.displayName,
                    color = if (uiState.category == category) ZariyaTextOnPrimary else ZariyaTextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp,
                    fontWeight = if (uiState.category == category) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = uiState.name,
        onValueChange = { viewModel.updateState(uiState.copy(name = it)) },
        label = { Text("Medicine Name*") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaPrimary,
            focusedLabelColor = ZariyaPrimary,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            focusedContainerColor = ZariyaBackground
        )
    )

    OutlinedTextField(
        value = uiState.genericName,
        onValueChange = { viewModel.updateState(uiState.copy(genericName = it)) },
        label = { Text("Generic Name (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaPrimary,
            focusedLabelColor = ZariyaPrimary,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            focusedContainerColor = ZariyaBackground
        )
    )

    OutlinedTextField(
        value = uiState.manufacturer,
        onValueChange = { viewModel.updateState(uiState.copy(manufacturer = it)) },
        label = { Text("Manufacturer (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaPrimary,
            focusedLabelColor = ZariyaPrimary,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            focusedContainerColor = ZariyaBackground
        )
    )
}

@Composable
fun StepDosage(uiState: AddEditMedicineUiState, viewModel: AddEditMedicineViewModel) {
    Text(
        text = "Dosage Details",
        color = ZariyaTextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
    
    Text(
        text = "How much per dose?",
        color = ZariyaTextSecondary,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.dosage,
            onValueChange = { viewModel.updateState(uiState.copy(dosage = it)) },
            label = { Text("Dosage Amount*") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZariyaPrimary,
                focusedLabelColor = ZariyaPrimary,
                unfocusedBorderColor = ZariyaSurfaceElevated,
                unfocusedTextColor = ZariyaTextPrimary,
                focusedTextColor = ZariyaTextPrimary,
                unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
                focusedContainerColor = ZariyaBackground
            )
        )

        OutlinedTextField(
            value = uiState.dosageUnit,
            onValueChange = { viewModel.updateState(uiState.copy(dosageUnit = it)) },
            label = { Text("Unit") },
            modifier = Modifier.width(120.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZariyaPrimary,
                focusedLabelColor = ZariyaPrimary,
                unfocusedBorderColor = ZariyaSurfaceElevated,
                unfocusedTextColor = ZariyaTextPrimary,
                focusedTextColor = ZariyaTextPrimary,
                unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
                focusedContainerColor = ZariyaBackground
            )
        )
    }
}

@Composable
fun StepSchedule(uiState: AddEditMedicineUiState, viewModel: AddEditMedicineViewModel) {
    Text(
        text = "Prescription Details",
        color = ZariyaTextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
    
    Text(
        text = "Any notes from the doctor?",
        color = ZariyaTextSecondary,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = uiState.doctorName,
        onValueChange = { viewModel.updateState(uiState.copy(doctorName = it)) },
        label = { Text("Doctor's Name (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaPrimary,
            focusedLabelColor = ZariyaPrimary,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            focusedContainerColor = ZariyaBackground
        )
    )

    OutlinedTextField(
        value = uiState.prescriptionNotes,
        onValueChange = { viewModel.updateState(uiState.copy(prescriptionNotes = it)) },
        label = { Text("Prescription Instructions (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaPrimary,
            focusedLabelColor = ZariyaPrimary,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            focusedContainerColor = ZariyaBackground
        )
    )
    
    OutlinedTextField(
        value = uiState.notes,
        onValueChange = { viewModel.updateState(uiState.copy(notes = it)) },
        label = { Text("Personal Notes (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaPrimary,
            focusedLabelColor = ZariyaPrimary,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            focusedContainerColor = ZariyaBackground
        )
    )
}

@Composable
fun StepInventory(uiState: AddEditMedicineUiState, viewModel: AddEditMedicineViewModel) {
    Text(
        text = "Inventory Tracking",
        color = ZariyaTextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
    
    Text(
        text = "Keep track of your supply so you never run out.",
        color = ZariyaTextSecondary,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    ZariyaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Track Stock",
                        fontWeight = FontWeight.SemiBold,
                        color = ZariyaTextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Receive low stock alerts",
                        color = ZariyaTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = uiState.isStockTracked,
                    onCheckedChange = { viewModel.updateState(uiState.copy(isStockTracked = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ZariyaBackground,
                        checkedTrackColor = ZariyaPrimary
                    )
                )
            }

            AnimatedVisibility(visible = uiState.isStockTracked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isLiquid = uiState.form == MedicineForm.SYRUP || 
                                   uiState.form == MedicineForm.DROPS || 
                                   uiState.form == MedicineForm.INJECTION

                    if (isLiquid) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.totalVolume,
                                onValueChange = { viewModel.updateState(uiState.copy(totalVolume = it)) },
                                label = { Text("Total Volume (ml)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ZariyaPrimary,
                                    focusedLabelColor = ZariyaPrimary,
                                    unfocusedBorderColor = ZariyaSurfaceElevated,
                                    unfocusedTextColor = ZariyaTextPrimary,
                                    focusedTextColor = ZariyaTextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = uiState.volumePerDose,
                                onValueChange = { viewModel.updateState(uiState.copy(volumePerDose = it)) },
                                label = { Text("Per dose (ml)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ZariyaPrimary,
                                    focusedLabelColor = ZariyaPrimary,
                                    unfocusedBorderColor = ZariyaSurfaceElevated,
                                    unfocusedTextColor = ZariyaTextPrimary,
                                    focusedTextColor = ZariyaTextPrimary
                                )
                            )
                        }
                    } else {
                        val stockLabel = when (uiState.form) {
                            MedicineForm.OTHER -> "Current Stock Count"
                            else -> "Total ${uiState.form.displayName}s"
                        }
                        OutlinedTextField(
                            value = uiState.stockCount,
                            onValueChange = { viewModel.updateState(uiState.copy(stockCount = it)) },
                            label = { Text(stockLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZariyaPrimary,
                                focusedLabelColor = ZariyaPrimary,
                                unfocusedBorderColor = ZariyaSurfaceElevated,
                                unfocusedTextColor = ZariyaTextPrimary,
                                focusedTextColor = ZariyaTextPrimary
                            )
                        )
                    }
                    
                    OutlinedTextField(
                        value = uiState.stockAlertThreshold,
                        onValueChange = { viewModel.updateState(uiState.copy(stockAlertThreshold = it)) },
                        label = { Text("Alert Threshold") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZariyaPrimary,
                            focusedLabelColor = ZariyaPrimary,
                            unfocusedBorderColor = ZariyaSurfaceElevated,
                            unfocusedTextColor = ZariyaTextPrimary,
                            focusedTextColor = ZariyaTextPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StepReview(uiState: AddEditMedicineUiState, viewModel: AddEditMedicineViewModel) {
    Text(
        text = "Review & Save",
        color = ZariyaTextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
    
    Text(
        text = "Double check the details.",
        color = ZariyaTextSecondary,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (uiState.error != null) {
        Text(
            text = uiState.error!!,
            color = ZariyaError,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    ZariyaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ZariyaPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    MedicineAvatar(form = uiState.form.name, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = uiState.name.ifEmpty { "Unnamed Medicine" },
                        color = ZariyaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (uiState.genericName.isNotBlank()) {
                        Text(
                            text = uiState.genericName,
                            color = ZariyaTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            HorizontalDivider(color = ZariyaSurfaceElevated)
            
            ReviewItem("Dosage", "${uiState.dosage} ${uiState.dosageUnit}")
            ReviewItem("Category", uiState.category.displayName)
            
            if (uiState.isStockTracked) {
                HorizontalDivider(color = ZariyaSurfaceElevated)
                val isLiquid = uiState.form == MedicineForm.SYRUP || uiState.form == MedicineForm.DROPS || uiState.form == MedicineForm.INJECTION
                if (isLiquid) {
                    ReviewItem("Total Volume", "${uiState.totalVolume} ml")
                } else {
                    ReviewItem("Current Stock", uiState.stockCount)
                }
            }
            
            if (uiState.doctorName.isNotBlank()) {
                HorizontalDivider(color = ZariyaSurfaceElevated)
                ReviewItem("Doctor", uiState.doctorName)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ZariyaPrimary.copy(alpha = 0.05f))
            .clickable { viewModel.updateState(uiState.copy(setReminder = !uiState.setReminder)) }
            .padding(16.dp)
    ) {
        Checkbox(
            checked = uiState.setReminder,
            onCheckedChange = { viewModel.updateState(uiState.copy(setReminder = it)) },
            colors = CheckboxDefaults.colors(
                checkedColor = ZariyaPrimary,
                checkmarkColor = ZariyaBackground
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Set a reminder for this medicine",
            color = ZariyaTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun ReviewItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = ZariyaTextSecondary, fontSize = 14.sp)
        Text(text = value.ifEmpty { "-" }, color = ZariyaTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun FormSelector(
    form: MedicineForm,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) ZariyaPrimary.copy(alpha = 0.2f) else ZariyaSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            MedicineAvatar(
                form = form.name,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = form.displayName,
            color = if (isSelected) ZariyaPrimary else ZariyaTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
