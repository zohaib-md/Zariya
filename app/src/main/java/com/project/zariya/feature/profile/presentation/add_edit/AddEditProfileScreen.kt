package com.project.zariya.feature.profile.presentation.add_edit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.core.ui.components.ZariyaPrimaryButton
import com.project.zariya.core.ui.theme.*
import com.project.zariya.feature.profile.domain.model.ProfileType
import com.project.zariya.feature.profile.presentation.ProfileTypeChip
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    val totalSteps = 7
    val pagerState = rememberPagerState(pageCount = { totalSteps })

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val titles = listOf(
        "What's your name?",
        "Who is this profile for?",
        "What's your age?",
        "How tall are you?",
        "What's your weight?",
        "Any medical conditions?",
        "Any allergies?"
    )

    Scaffold(
        containerColor = ZariyaBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = ZariyaTextPrimary
                            )
                        }
                    },
                    actions = {
                        Text(
                            text = "${pagerState.currentPage + 1} / $totalSteps",
                            color = ZariyaTextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ZariyaBackground
                    )
                )
                
                // Progress Bar
                val progress by animateFloatAsState(
                    targetValue = (pagerState.currentPage + 1) / totalSteps.toFloat(),
                    label = "progress"
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ZariyaGradientStart,
                    trackColor = ZariyaSurfaceElevated
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                ZariyaPrimaryButton(
                    text = if (pagerState.currentPage == totalSteps - 1) "Save Profile" else "Continue",
                    onClick = {
                        if (pagerState.currentPage < totalSteps - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.saveProfile()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    isLoading = uiState.isLoading
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = titles[pagerState.currentPage],
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ZariyaTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (page) {
                        0 -> NameStep(
                            name = uiState.name,
                            onNameChange = { viewModel.updateState(uiState.copy(name = it)) }
                        )
                        1 -> TypeStep(
                            type = uiState.type,
                            onTypeChange = { viewModel.updateState(uiState.copy(type = it)) }
                        )
                        2 -> AgeStep(
                            age = uiState.age,
                            onAgeChange = { viewModel.updateState(uiState.copy(age = it)) }
                        )
                        3 -> HeightStep(
                            height = uiState.height,
                            onHeightChange = { viewModel.updateState(uiState.copy(height = it)) }
                        )
                        4 -> WeightStep(
                            weight = uiState.weight,
                            onWeightChange = { viewModel.updateState(uiState.copy(weight = it)) }
                        )
                        5 -> MedicalConditionsStep(
                            conditions = uiState.medicalConditions,
                            onConditionsChange = { viewModel.updateState(uiState.copy(medicalConditions = it)) }
                        )
                        6 -> AllergiesStep(
                            allergies = uiState.allergies,
                            onAllergiesChange = { viewModel.updateState(uiState.copy(allergies = it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = { Text("e.g. John Doe", color = ZariyaTextSecondary.copy(alpha = 0.5f)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaGradientStart,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun TypeStep(type: ProfileType, onTypeChange: (ProfileType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileTypeChip(
                type = ProfileType.SELF,
                isSelected = type == ProfileType.SELF,
                onClick = { onTypeChange(ProfileType.SELF) },
                modifier = Modifier.weight(1f)
            )
            ProfileTypeChip(
                type = ProfileType.PARENT,
                isSelected = type == ProfileType.PARENT,
                onClick = { onTypeChange(ProfileType.PARENT) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileTypeChip(
                type = ProfileType.CHILD,
                isSelected = type == ProfileType.CHILD,
                onClick = { onTypeChange(ProfileType.CHILD) },
                modifier = Modifier.weight(1f)
            )
            ProfileTypeChip(
                type = ProfileType.CUSTOM,
                isSelected = type == ProfileType.CUSTOM,
                onClick = { onTypeChange(ProfileType.CUSTOM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AgeStep(age: String, onAgeChange: (String) -> Unit) {
    val items = (1..120).map { it.toString() }
    WheelPicker(
        items = items,
        selectedValue = age.takeIf { it.isNotBlank() } ?: "25",
        onValueChange = onAgeChange,
        label = "years",
        modifier = Modifier.padding(top = 32.dp)
    )
}

@Composable
private fun HeightStep(height: String, onHeightChange: (String) -> Unit) {
    val currentHeight = height.toIntOrNull() ?: 170
    HorizontalRulerPicker(
        range = 50..250,
        selectedValue = currentHeight,
        onValueChange = { onHeightChange(it.toString()) },
        unit = "cm",
        modifier = Modifier.padding(top = 64.dp)
    )
}

@Composable
private fun WeightStep(weight: String, onWeightChange: (String) -> Unit) {
    val currentWeight = weight.toFloatOrNull()?.toInt() ?: 65
    HorizontalRulerPicker(
        range = 20..200,
        selectedValue = currentWeight,
        onValueChange = { onWeightChange(it.toString()) },
        unit = "kg",
        modifier = Modifier.padding(top = 64.dp)
    )
}

@Composable
private fun MedicalConditionsStep(conditions: String, onConditionsChange: (String) -> Unit) {
    OutlinedTextField(
        value = conditions,
        onValueChange = onConditionsChange,
        placeholder = { Text("e.g. Diabetes, Hypertension (Optional)") },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaGradientStart,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AllergiesStep(allergies: String, onAllergiesChange: (String) -> Unit) {
    OutlinedTextField(
        value = allergies,
        onValueChange = onAllergiesChange,
        placeholder = { Text("e.g. Penicillin, Peanuts (Optional)") },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZariyaGradientStart,
            unfocusedBorderColor = ZariyaSurfaceElevated,
            focusedTextColor = ZariyaTextPrimary,
            unfocusedTextColor = ZariyaTextPrimary,
            focusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.5f),
            unfocusedContainerColor = ZariyaSurfaceElevated.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}
