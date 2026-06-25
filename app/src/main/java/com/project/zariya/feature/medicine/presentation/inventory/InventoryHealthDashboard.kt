package com.project.zariya.feature.medicine.presentation.inventory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.theme.*
import com.project.zariya.feature.medicine.domain.model.Medicine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryHealthDashboard(
    onNavigateBack: () -> Unit,
    onMedicineClick: (String) -> Unit,
    viewModel: InventoryHealthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ZariyaBackground,
        topBar = {
            TopAppBar(
                title = { Text("Inventory Health", color = ZariyaTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = ZariyaPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ZariyaBackground)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ZariyaPrimary)
            }
        } else if (uiState.trackedMedicines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No tracked medicines found.", color = ZariyaTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                if (uiState.criticalMedicines.isNotEmpty()) {
                    item {
                        InventorySection(
                            title = "Critical (Refill Soon)",
                            medicines = uiState.criticalMedicines,
                            color = ZariyaError,
                            onClick = onMedicineClick
                        )
                    }
                }

                if (uiState.mediumMedicines.isNotEmpty()) {
                    item {
                        InventorySection(
                            title = "Running Low",
                            medicines = uiState.mediumMedicines,
                            color = Color(0xFFF59E0B), // Amber
                            onClick = onMedicineClick
                        )
                    }
                }

                if (uiState.healthyMedicines.isNotEmpty()) {
                    item {
                        InventorySection(
                            title = "Well Stocked",
                            medicines = uiState.healthyMedicines,
                            color = ZariyaSuccess,
                            onClick = onMedicineClick
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun InventorySection(
    title: String,
    medicines: List<Medicine>,
    color: Color,
    onClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = ZariyaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        medicines.forEach { medicine ->
            InventoryMedicineCard(medicine = medicine, ringColor = color, onClick = { onClick(medicine.id) })
        }
    }
}

@Composable
fun InventoryMedicineCard(
    medicine: Medicine,
    ringColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = ZariyaSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated ring based on alert threshold
            val maxExpected = (medicine.stockAlertThreshold * 3f).coerceAtLeast(1f)
            val targetProgress = (medicine.currentStock / maxExpected).coerceIn(0f, 1f)
            val progress by animateFloatAsState(
                targetValue = targetProgress,
                animationSpec = tween(1000),
                label = "progress"
            )

            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    color = ZariyaSurfaceElevated,
                    strokeWidth = 5.dp,
                    modifier = Modifier.fillMaxSize()
                )
                CircularProgressIndicator(
                    progress = { progress },
                    color = ringColor,
                    strokeWidth = 5.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${medicine.currentStock}",
                        color = ZariyaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    color = ZariyaTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${medicine.dosage} ${medicine.dosageUnit} • ${medicine.currentStock} ${medicine.currentStockUnit} left",
                    color = ZariyaTextSecondary,
                    fontSize = 14.sp
                )
            }
            
            // Refill Action
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ZariyaPrimary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Refill",
                    tint = ZariyaPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
