package com.project.zariya.feature.interaction.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.EmptyState
import com.project.zariya.core.ui.components.LoadingState
import com.project.zariya.core.ui.components.ZariyaCard
import com.project.zariya.core.ui.components.ZariyaPrimaryButton
import com.project.zariya.core.ui.theme.SeverityMild
import com.project.zariya.core.ui.theme.SeverityModerate
import com.project.zariya.core.ui.theme.SeveritySevere
import com.project.zariya.core.ui.theme.ZariyaBackground
import com.project.zariya.core.ui.theme.ZariyaPrimary
import com.project.zariya.core.ui.theme.ZariyaSurface
import com.project.zariya.core.ui.theme.ZariyaSurfaceVariant
import com.project.zariya.core.ui.theme.ZariyaTextPrimary
import com.project.zariya.core.ui.theme.ZariyaTextSecondary
import com.project.zariya.feature.interaction.domain.model.DrugInteraction
import com.project.zariya.feature.interaction.domain.model.InteractionSeverity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionScreen(
    onNavigateBack: () -> Unit,
    viewModel: InteractionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            val result = snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = "Retry"
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.checkAllInteractions()
            }
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = ZariyaBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Drug Interactions",
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
        ) {
            // Check All button
            ZariyaPrimaryButton(
                text = "Check All Medicines",
                onClick = { viewModel.checkAllInteractions() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                isLoading = uiState.isLoading,
                icon = Icons.Default.Refresh
            )

            // Content area
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) {
                        LoadingState(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (uiState.interactions.isEmpty() && uiState.error == null) {
                    EmptyState(
                        icon = Icons.Default.CheckCircle,
                        title = "No Known Interactions",
                        subtitle = "Tap \"Check All Medicines\" to scan your active medicines for potential drug interactions."
                    )
                } else {
                    InteractionResultsList(interactions = uiState.interactions)
                }
            }
        }
    }
}

@Composable
private fun InteractionResultsList(
    interactions: List<DrugInteraction>
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary header
        item {
            InteractionSummaryHeader(interactions = interactions)
        }

        items(
            items = interactions,
            key = { "${it.medicineName1}|${it.medicineName2}|${it.description.hashCode()}" }
        ) { interaction ->
            InteractionCard(interaction = interaction)
        }

        // Bottom spacer for nav bar clearance
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InteractionSummaryHeader(
    interactions: List<DrugInteraction>
) {
    val severeCount = interactions.count { it.severity == InteractionSeverity.SEVERE }
    val moderateCount = interactions.count { it.severity == InteractionSeverity.MODERATE }
    val mildCount = interactions.count { it.severity == InteractionSeverity.MILD }
    val unknownCount = interactions.count { it.severity == InteractionSeverity.UNKNOWN }

    ZariyaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Interaction Summary",
                color = ZariyaTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryChip(label = "Severe", count = severeCount, severity = InteractionSeverity.SEVERE)
                SummaryChip(label = "Moderate", count = moderateCount, severity = InteractionSeverity.MODERATE)
                SummaryChip(label = "Mild", count = mildCount, severity = InteractionSeverity.MILD)
                if (unknownCount > 0) {
                    SummaryChip(label = "Unknown", count = unknownCount, severity = InteractionSeverity.UNKNOWN)
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    count: Int,
    severity: InteractionSeverity
) {
    val color = when (severity) {
        InteractionSeverity.MILD -> SeverityMild
        InteractionSeverity.MODERATE -> SeverityModerate
        InteractionSeverity.SEVERE -> SeveritySevere
        InteractionSeverity.UNKNOWN -> ZariyaTextSecondary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = color.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun InteractionCard(
    interaction: DrugInteraction
) {
    ZariyaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Drug names row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = interaction.medicineName1,
                        color = ZariyaTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = ZariyaTextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "interacts with",
                            color = ZariyaTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = interaction.medicineName2,
                        color = ZariyaTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Severity badge
                SeverityBadge(severity = interaction.severity)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = interaction.description,
                color = ZariyaTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Source
            Text(
                text = "Source: ${interaction.source}",
                color = ZariyaTextSecondary.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SeverityBadge(
    severity: InteractionSeverity
) {
    val backgroundColor = when (severity) {
        InteractionSeverity.MILD -> SeverityMild
        InteractionSeverity.MODERATE -> SeverityModerate
        InteractionSeverity.SEVERE -> SeveritySevere
        InteractionSeverity.UNKNOWN -> ZariyaSurfaceVariant
    }

    val textColor = when (severity) {
        InteractionSeverity.UNKNOWN -> ZariyaTextSecondary
        else -> ZariyaSurface
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor.copy(alpha = 0.85f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = severity.displayName,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
