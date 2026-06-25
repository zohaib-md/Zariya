package com.project.zariya.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.project.zariya.R
import com.project.zariya.core.ui.components.ZariyaCard
import com.project.zariya.core.ui.components.ZariyaPrimaryButton
import com.project.zariya.core.ui.theme.*
import com.project.zariya.feature.profile.domain.model.ProfileType
import com.project.zariya.feature.profile.domain.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
    onNavigateToAddProfile: () -> Unit,
    onNavigateToEditProfile: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Profiles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout, 
                            contentDescription = "Logout",
                            tint = ZariyaPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ZariyaBackground,
                    titleContentColor = ZariyaTextPrimary,
                    navigationIconContentColor = ZariyaTextPrimary
                )
            )
        },
        floatingActionButton = {
            if (uiState.profiles.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onNavigateToAddProfile,
                    containerColor = ZariyaPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Profile")
                }
            }
        },
        containerColor = ZariyaBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = ZariyaPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.profiles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.profile_user_card))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(250.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "No Profiles Yet",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZariyaTextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Set up your first profile to personalize your health and medication tracking.",
                        fontSize = 16.sp,
                        color = ZariyaTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    ZariyaPrimaryButton(
                        text = "Set up your profile",
                        onClick = onNavigateToAddProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.profiles) { profile ->
                        ProfileItemCard(
                            profile = profile,
                            isActive = profile.id == uiState.activeProfile?.id,
                            onClick = { viewModel.switchProfile(profile.id) },
                            onEditClick = { onNavigateToEditProfile(profile.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItemCard(
    profile: UserProfile,
    isActive: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    ZariyaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isActive) ZariyaPrimary else ZariyaSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = if (isActive) Color.White else ZariyaTextSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZariyaTextPrimary
                )
                Text(
                    text = profile.profileType.displayName,
                    fontSize = 14.sp,
                    color = ZariyaTextSecondary
                )
            }
            
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active Profile",
                    tint = ZariyaPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = ZariyaPrimary
                )
            }
        }
    }
}

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ProfileType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ProfileType.SELF) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ZariyaSurface,
        titleContentColor = ZariyaTextPrimary,
        textContentColor = ZariyaTextSecondary,
        title = {
            Text(text = "Add New Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZariyaPrimary,
                        focusedLabelColor = ZariyaPrimary,
                        unfocusedBorderColor = ZariyaTextTertiary,
                        unfocusedLabelColor = ZariyaTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Profile Type", fontWeight = FontWeight.SemiBold, color = ZariyaTextPrimary)

                // Simple chip selection for profile type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileTypeChip(
                        type = ProfileType.SELF,
                        isSelected = selectedType == ProfileType.SELF,
                        onClick = { selectedType = ProfileType.SELF }
                    )
                    ProfileTypeChip(
                        type = ProfileType.PARENT,
                        isSelected = selectedType == ProfileType.PARENT,
                        onClick = { selectedType = ProfileType.PARENT }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileTypeChip(
                        type = ProfileType.CHILD,
                        isSelected = selectedType == ProfileType.CHILD,
                        onClick = { selectedType = ProfileType.CHILD }
                    )
                    ProfileTypeChip(
                        type = ProfileType.CUSTOM,
                        isSelected = selectedType == ProfileType.CUSTOM,
                        onClick = { selectedType = ProfileType.CUSTOM }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedType)
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = ZariyaPrimary)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ZariyaTextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileTypeChip(
    type: ProfileType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) ZariyaPrimary.copy(alpha = 0.2f) else ZariyaSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = type.displayName,
            color = if (isSelected) ZariyaPrimary else ZariyaTextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
