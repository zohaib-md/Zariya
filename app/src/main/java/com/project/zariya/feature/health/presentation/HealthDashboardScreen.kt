package com.project.zariya.feature.health.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.EmptyState
import com.project.zariya.core.ui.components.LoadingState
import com.project.zariya.core.ui.components.ZariyaCard
import com.project.zariya.core.ui.components.ZariyaPrimaryButton
import com.project.zariya.core.ui.theme.ZariyaBackground
import com.project.zariya.core.ui.theme.ZariyaError
import com.project.zariya.core.ui.theme.ZariyaPrimary
import com.project.zariya.core.ui.theme.ZariyaSuccess
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaSurfaceVariant
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import com.project.zariya.core.ui.theme.ZariyaWarning
import com.project.zariya.feature.health.domain.model.HealthMetricType
import com.project.zariya.feature.health.domain.model.HealthRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: HealthDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.onPermissionsResult()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health Dashboard",
                        color = ZariyaTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = ZariyaTextPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.isAvailable && uiState.hasPermission) {
                        IconButton(
                            onClick = { viewModel.refreshData() },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync data",
                                tint = ZariyaPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZariyaBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ZariyaBackground
    ) { paddingValues ->
        when {
            !uiState.isAvailable -> {
                HealthConnectUnavailableContent(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            !uiState.hasPermission -> {
                PermissionRequestContent(
                    modifier = Modifier.padding(paddingValues),
                    onRequestPermissions = {
                        val permissions = viewModel.requestPermissions()
                        permissionLauncher.launch(permissions)
                    }
                )
            }
            uiState.isLoading && uiState.latestMetrics.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    repeat(4) {
                        LoadingState(modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            else -> {
                HealthDashboardContent(
                    uiState = uiState,
                    modifier = Modifier.padding(paddingValues),
                    onSyncClick = { viewModel.refreshData() }
                )
            }
        }
    }
}

@Composable
private fun HealthConnectUnavailableContent(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Default.FavoriteBorder,
        title = "Health Connect Not Available",
        subtitle = "Health Connect is required to sync your health data. Please install Health Connect from the Google Play Store.",
        modifier = modifier,
        action = {
            ZariyaPrimaryButton(
                text = "Open Play Store",
                onClick = { /* Intent to Play Store handled by caller */ }
            )
        }
    )
}

@Composable
private fun PermissionRequestContent(
    modifier: Modifier = Modifier,
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ZariyaCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = ZariyaPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Connect Your Health Data",
                    color = ZariyaTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Grant permissions to read your blood pressure, weight, blood glucose, and heart rate data from Health Connect.",
                    color = ZariyaTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                ZariyaPrimaryButton(
                    text = "Grant Permissions",
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HealthDashboardContent(
    uiState: HealthDashboardUiState,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Latest Metrics",
                color = ZariyaTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            MetricsGrid(latestMetrics = uiState.latestMetrics)
        }

        if (uiState.bloodPressureHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Blood Pressure History",
                    color = ZariyaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                BloodPressureChart(
                    records = uiState.bloodPressureHistory.takeLast(14)
                )
            }
        }

        if (uiState.weightHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Weight Trend",
                    color = ZariyaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                WeightTrendChart(
                    records = uiState.weightHistory.takeLast(14)
                )
            }
        }

        if (uiState.heartRateHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Heart Rate History",
                    color = ZariyaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                HeartRateChart(
                    records = uiState.heartRateHistory.takeLast(20)
                )
            }
        }

        if (uiState.glucoseHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Blood Glucose History",
                    color = ZariyaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                GlucoseChart(
                    records = uiState.glucoseHistory.takeLast(14)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            ZariyaPrimaryButton(
                text = "Sync Health Data",
                onClick = onSyncClick,
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading,
                icon = Icons.Default.Refresh
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricsGrid(
    latestMetrics: Map<HealthMetricType, HealthRecord?>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                metricType = HealthMetricType.BLOOD_PRESSURE,
                record = latestMetrics[HealthMetricType.BLOOD_PRESSURE],
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                metricType = HealthMetricType.WEIGHT,
                record = latestMetrics[HealthMetricType.WEIGHT],
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                metricType = HealthMetricType.BLOOD_GLUCOSE,
                record = latestMetrics[HealthMetricType.BLOOD_GLUCOSE],
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                metricType = HealthMetricType.HEART_RATE,
                record = latestMetrics[HealthMetricType.HEART_RATE],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    metricType: HealthMetricType,
    record: HealthRecord?,
    modifier: Modifier = Modifier
) {
    val valueText = when (record) {
        is HealthRecord.BloodPressureRecord -> "${record.systolic.toInt()}/${record.diastolic.toInt()}"
        is HealthRecord.WeightRecord -> String.format(Locale.US, "%.1f", record.weight)
        is HealthRecord.BloodGlucoseRecord -> String.format(Locale.US, "%.0f", record.level)
        is HealthRecord.HeartRateRecord -> "${record.bpm}"
        null -> "--"
    }

    val statusColor = when (record) {
        is HealthRecord.BloodPressureRecord -> getBpStatusColor(record.systolic, record.diastolic)
        is HealthRecord.HeartRateRecord -> getHrStatusColor(record.bpm)
        is HealthRecord.BloodGlucoseRecord -> getGlucoseStatusColor(record.level)
        else -> ZariyaPrimary
    }

    val timestampText = record?.let {
        formatTimestamp(it.timestamp)
    } ?: "No data"

    val trendArrow = getTrendArrow(record)

    ZariyaCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = metricType.displayName,
                    color = ZariyaTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = valueText,
                    color = ZariyaTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                if (trendArrow.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trendArrow,
                        color = statusColor,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = metricType.unit,
                color = ZariyaTextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = timestampText,
                color = ZariyaTextSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BloodPressureChart(
    records: List<HealthRecord.BloodPressureRecord>
) {
    if (records.isEmpty()) return

    val sortedRecords = records.sortedBy { it.timestamp }

    ZariyaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val padding = 40f

                val drawableWidth = chartWidth - padding * 2
                val drawableHeight = chartHeight - padding * 2

                val allValues = sortedRecords.flatMap { listOf(it.systolic, it.diastolic) }
                val maxVal = (allValues.maxOrNull() ?: 180.0).coerceAtLeast(140.0)
                val minVal = (allValues.minOrNull() ?: 60.0).coerceAtMost(60.0)
                val valueRange = (maxVal - minVal).coerceAtLeast(1.0)

                // Draw grid lines
                val gridLineCount = 4
                for (i in 0..gridLineCount) {
                    val y = padding + drawableHeight * i / gridLineCount
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(padding, y),
                        end = Offset(chartWidth - padding, y),
                        strokeWidth = 1f
                    )
                    val labelValue = maxVal - (valueRange * i / gridLineCount)
                    drawContext.canvas.nativeCanvas.drawText(
                        "${labelValue.toInt()}",
                        4f,
                        y + 4f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(150, 139, 148, 158)
                            textSize = 24f
                            isAntiAlias = true
                        }
                    )
                }

                if (sortedRecords.size >= 2) {
                    // Systolic line
                    val systolicPath = Path()
                    sortedRecords.forEachIndexed { index, record ->
                        val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                        val y = padding + drawableHeight * (1 - (record.systolic - minVal) / valueRange).toFloat()
                        if (index == 0) systolicPath.moveTo(x, y) else systolicPath.lineTo(x, y)
                    }
                    drawPath(
                        path = systolicPath,
                        color = Color(0xFFEF5350),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // Diastolic line
                    val diastolicPath = Path()
                    sortedRecords.forEachIndexed { index, record ->
                        val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                        val y = padding + drawableHeight * (1 - (record.diastolic - minVal) / valueRange).toFloat()
                        if (index == 0) diastolicPath.moveTo(x, y) else diastolicPath.lineTo(x, y)
                    }
                    drawPath(
                        path = diastolicPath,
                        color = Color(0xFF42A5F5),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }

                // Draw data points
                sortedRecords.forEachIndexed { index, record ->
                    val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                    val systolicY = padding + drawableHeight * (1 - (record.systolic - minVal) / valueRange).toFloat()
                    val diastolicY = padding + drawableHeight * (1 - (record.diastolic - minVal) / valueRange).toFloat()

                    drawCircle(
                        color = Color(0xFFEF5350),
                        radius = 5f,
                        center = Offset(x, systolicY)
                    )
                    drawCircle(
                        color = Color(0xFF42A5F5),
                        radius = 5f,
                        center = Offset(x, diastolicY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFFEF5350), androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Systolic",
                    color = ZariyaTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(24.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF42A5F5), androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Diastolic",
                    color = ZariyaTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun WeightTrendChart(
    records: List<HealthRecord.WeightRecord>
) {
    if (records.isEmpty()) return

    val sortedRecords = records.sortedBy { it.timestamp }

    ZariyaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val padding = 40f

                val drawableWidth = chartWidth - padding * 2
                val drawableHeight = chartHeight - padding * 2

                val weights = sortedRecords.map { it.weight }
                val maxVal = (weights.maxOrNull() ?: 100.0) + 2.0
                val minVal = (weights.minOrNull() ?: 40.0) - 2.0
                val valueRange = (maxVal - minVal).coerceAtLeast(1.0)

                // Draw grid lines
                val gridLineCount = 3
                for (i in 0..gridLineCount) {
                    val y = padding + drawableHeight * i / gridLineCount
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(padding, y),
                        end = Offset(chartWidth - padding, y),
                        strokeWidth = 1f
                    )
                    val labelValue = maxVal - (valueRange * i / gridLineCount)
                    drawContext.canvas.nativeCanvas.drawText(
                        String.format(Locale.US, "%.0f", labelValue),
                        4f,
                        y + 4f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(150, 139, 148, 158)
                            textSize = 24f
                            isAntiAlias = true
                        }
                    )
                }

                if (sortedRecords.size >= 2) {
                    val weightPath = Path()
                    sortedRecords.forEachIndexed { index, record ->
                        val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                        val y = padding + drawableHeight * (1 - (record.weight - minVal) / valueRange).toFloat()
                        if (index == 0) weightPath.moveTo(x, y) else weightPath.lineTo(x, y)
                    }
                    drawPath(
                        path = weightPath,
                        color = Color(0xFF00BFA6),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }

                sortedRecords.forEachIndexed { index, record ->
                    val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                    val y = padding + drawableHeight * (1 - (record.weight - minVal) / valueRange).toFloat()
                    drawCircle(
                        color = Color(0xFF00BFA6),
                        radius = 5f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeartRateChart(
    records: List<HealthRecord.HeartRateRecord>
) {
    if (records.isEmpty()) return

    val sortedRecords = records.sortedBy { it.timestamp }

    ZariyaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val padding = 40f

                val drawableWidth = chartWidth - padding * 2
                val drawableHeight = chartHeight - padding * 2

                val bpms = sortedRecords.map { it.bpm.toDouble() }
                val maxVal = (bpms.maxOrNull() ?: 120.0).coerceAtLeast(100.0)
                val minVal = (bpms.minOrNull() ?: 60.0).coerceAtMost(50.0)
                val valueRange = (maxVal - minVal).coerceAtLeast(1.0)

                // Draw grid lines
                val gridLineCount = 3
                for (i in 0..gridLineCount) {
                    val y = padding + drawableHeight * i / gridLineCount
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(padding, y),
                        end = Offset(chartWidth - padding, y),
                        strokeWidth = 1f
                    )
                    val labelValue = maxVal - (valueRange * i / gridLineCount)
                    drawContext.canvas.nativeCanvas.drawText(
                        "${labelValue.toInt()}",
                        4f,
                        y + 4f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(150, 139, 148, 158)
                            textSize = 24f
                            isAntiAlias = true
                        }
                    )
                }

                if (sortedRecords.size >= 2) {
                    val path = Path()
                    sortedRecords.forEachIndexed { index, record ->
                        val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                        val y = padding + drawableHeight * (1 - (record.bpm - minVal) / valueRange).toFloat()
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFEF5350),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }

                sortedRecords.forEachIndexed { index, record ->
                    val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                    val y = padding + drawableHeight * (1 - (record.bpm - minVal) / valueRange).toFloat()
                    drawCircle(
                        color = Color(0xFFEF5350),
                        radius = 5f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlucoseChart(
    records: List<HealthRecord.BloodGlucoseRecord>
) {
    if (records.isEmpty()) return

    val sortedRecords = records.sortedBy { it.timestamp }

    ZariyaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val padding = 40f

                val drawableWidth = chartWidth - padding * 2
                val drawableHeight = chartHeight - padding * 2

                val levels = sortedRecords.map { it.level }
                val maxVal = (levels.maxOrNull() ?: 200.0).coerceAtLeast(140.0)
                val minVal = (levels.minOrNull() ?: 80.0).coerceAtMost(70.0)
                val valueRange = (maxVal - minVal).coerceAtLeast(1.0)

                // Draw grid lines
                val gridLineCount = 3
                for (i in 0..gridLineCount) {
                    val y = padding + drawableHeight * i / gridLineCount
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(padding, y),
                        end = Offset(chartWidth - padding, y),
                        strokeWidth = 1f
                    )
                    val labelValue = maxVal - (valueRange * i / gridLineCount)
                    drawContext.canvas.nativeCanvas.drawText(
                        "${labelValue.toInt()}",
                        4f,
                        y + 4f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(150, 139, 148, 158)
                            textSize = 24f
                            isAntiAlias = true
                        }
                    )
                }

                if (sortedRecords.size >= 2) {
                    val path = Path()
                    sortedRecords.forEachIndexed { index, record ->
                        val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                        val y = padding + drawableHeight * (1 - (record.level - minVal) / valueRange).toFloat()
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFF9800),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }

                sortedRecords.forEachIndexed { index, record ->
                    val x = padding + drawableWidth * index / (sortedRecords.size - 1).coerceAtLeast(1)
                    val y = padding + drawableHeight * (1 - (record.level - minVal) / valueRange).toFloat()
                    drawCircle(
                        color = Color(0xFFFF9800),
                        radius = 5f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

private fun getBpStatusColor(systolic: Double, diastolic: Double): Color {
    return when {
        systolic < 120 && diastolic < 80 -> Color(0xFF4CAF50) // Normal
        systolic < 130 && diastolic < 80 -> Color(0xFFFFAB40) // Elevated
        systolic < 140 || diastolic < 90 -> Color(0xFFFF9800) // High Stage 1
        else -> Color(0xFFEF5350) // High Stage 2
    }
}

private fun getHrStatusColor(bpm: Int): Color {
    return when {
        bpm in 60..100 -> Color(0xFF4CAF50) // Normal
        bpm in 50..59 || bpm in 101..110 -> Color(0xFFFFAB40) // Slightly off
        else -> Color(0xFFEF5350) // Concern
    }
}

private fun getGlucoseStatusColor(level: Double): Color {
    return when {
        level < 100 -> Color(0xFF4CAF50) // Normal
        level < 126 -> Color(0xFFFF9800) // Pre-diabetic
        else -> Color(0xFFEF5350) // Diabetic range
    }
}

private fun getTrendArrow(record: HealthRecord?): String {
    return when (record) {
        is HealthRecord.BloodPressureRecord -> when {
            record.systolic > 140 -> "↑"
            record.systolic < 90 -> "↓"
            else -> "→"
        }
        is HealthRecord.HeartRateRecord -> when {
            record.bpm > 100 -> "↑"
            record.bpm < 60 -> "↓"
            else -> "→"
        }
        is HealthRecord.BloodGlucoseRecord -> when {
            record.level > 126 -> "↑"
            record.level < 70 -> "↓"
            else -> "→"
        }
        is HealthRecord.WeightRecord -> "→"
        null -> ""
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
