package com.project.zariya.feature.for_her.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.feature.for_her.presentation.theme.MoonlitColors
import kotlinx.coroutines.delay
import java.util.Calendar

import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDateRangePickerState
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.zariya.feature.for_her.domain.util.CyclePhase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForHerHomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLuna: () -> Unit,
    viewModel: ForHerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showMoodSheet by remember { mutableStateOf(false) }
    var showSymptomSheet by remember { mutableStateOf(false) }
    var showCalendarSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showMoodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoodSheet = false },
            sheetState = sheetState,
            containerColor = MoonlitColors.BackgroundEnd
        ) {
            MoodSelectionSheet(onMoodSelected = { showMoodSheet = false })
        }
    }

    if (showSymptomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSymptomSheet = false },
            sheetState = sheetState,
            containerColor = MoonlitColors.BackgroundEnd
        ) {
            SymptomSelectionSheet(onSymptomsSaved = { showSymptomSheet = false })
        }
    }

    if (showCalendarSheet) {
        val dateRangePickerState = rememberDateRangePickerState()
        ModalBottomSheet(
            onDismissRequest = { showCalendarSheet = false },
            sheetState = sheetState,
            containerColor = MoonlitColors.BackgroundEnd,
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Log Your Period", color = MoonlitColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = {
                            val startMillis = dateRangePickerState.selectedStartDateMillis
                            if (startMillis != null) {
                                viewModel.saveCycleLog(startMillis, dateRangePickerState.selectedEndDateMillis)
                                showCalendarSheet = false
                            }
                        }
                    ) {
                        Text("Save", color = MoonlitColors.PrimaryAccent, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.DatePickerDefaults.colors(
                        containerColor = MoonlitColors.BackgroundEnd,
                        titleContentColor = MoonlitColors.TextPrimary,
                        headlineContentColor = MoonlitColors.TextPrimary,
                        weekdayContentColor = MoonlitColors.TextSecondary,
                        subheadContentColor = MoonlitColors.TextSecondary,
                        yearContentColor = MoonlitColors.TextPrimary,
                        currentYearContentColor = MoonlitColors.PrimaryAccent,
                        selectedYearContentColor = MoonlitColors.TextPrimary,
                        selectedYearContainerColor = MoonlitColors.PrimaryAccent,
                        dayContentColor = MoonlitColors.TextPrimary,
                        selectedDayContentColor = MoonlitColors.BackgroundStart,
                        selectedDayContainerColor = MoonlitColors.PrimaryAccent,
                        todayContentColor = MoonlitColors.SecondaryAccent,
                        todayDateBorderColor = MoonlitColors.SecondaryAccent
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("For Her", color = MoonlitColors.TextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MoonlitColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        // Rich Night Sky explicit gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MoonlitColors.BackgroundStart, MoonlitColors.BackgroundEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                GreetingSection()
                
                HeroMoonCard(
                    uiState = uiState,
                    onNavigateToLuna = onNavigateToLuna,
                    onLogPeriodClick = { showCalendarSheet = true }
                )
                
                MoodAndSymptomSection(
                    onMoodClick = { showMoodSheet = true },
                    onSymptomClick = { showSymptomSheet = true }
                )
                
                ComfortPicksSection()
                
                ReflectionSection()

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun MoodSelectionSheet(onMoodSelected: (String) -> Unit) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text("How are you feeling?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MoonlitColors.TextPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        
        val moods = listOf("Calm" to "😌", "Happy" to "😊", "Emotional" to "🥺", "Anxious" to "😟", "Energetic" to "🤩")
        
        moods.forEach { (mood, emoji) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMoodSelected(mood) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(mood, fontSize = 16.sp, color = MoonlitColors.TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SymptomSelectionSheet(onSymptomsSaved: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text("Log your symptoms", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MoonlitColors.TextPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        
        val symptoms = listOf("Cramps", "Bloating", "Fatigue", "Headache", "Tender Breasts")
        
        symptoms.forEach { symptom ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSymptomsSaved() } // For simplicity, clicking one saves it for now
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.LocalFlorist, contentDescription = null, tint = MoonlitColors.SecondaryAccent)
                Spacer(modifier = Modifier.width(16.dp))
                Text(symptom, fontSize = 16.sp, color = MoonlitColors.TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GreetingSection() {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning 🌙"
            hour < 17 -> "Good Afternoon 🌙"
            else -> "Good Evening 🌙"
        }
    }

    Text(
        text = greeting,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MoonlitColors.TextPrimary
    )
}

@Composable
private fun HeroMoonCard(
    uiState: ForHerUiState,
    onNavigateToLuna: () -> Unit,
    onLogPeriodClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "moonAnim")
    val moonGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MoonlitColors.Surface)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            // Moon visual
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        shadowElevation = moonGlow * 30f
                        shape = CircleShape
                        clip = false // don't clip the shadow
                    }
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text("🌙", fontSize = 72.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = uiState.currentPhase.displayName,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MoonlitColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = uiState.currentPhase.message,
                fontSize = 15.sp,
                color = MoonlitColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.currentPhase == CyclePhase.UNKNOWN) {
                    Button(
                        onClick = onLogPeriodClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MoonlitColors.SecondaryAccent),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text("Log Period", color = MoonlitColors.BackgroundStart, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // AI Coach Button
                Button(
                    onClick = onNavigateToLuna,
                    colors = ButtonDefaults.buttonColors(containerColor = MoonlitColors.PrimaryAccent),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = MoonlitColors.BackgroundStart, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat with Luna", color = MoonlitColors.BackgroundStart, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun MoodAndSymptomSection(
    onMoodClick: () -> Unit,
    onSymptomClick: () -> Unit
) {
    Column {
        Text("Log Your Day", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MoonlitColors.TextPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            LogCard(
                title = "Mood",
                icon = Icons.Outlined.SelfImprovement,
                color = MoonlitColors.MoodAccent,
                onClick = onMoodClick,
                modifier = Modifier.weight(1f)
            )
            LogCard(
                title = "Symptoms",
                icon = Icons.Outlined.LocalFlorist,
                color = MoonlitColors.SecondaryAccent,
                onClick = onSymptomClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LogCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MoonlitColors.Surface)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MoonlitColors.TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Medium, color = MoonlitColors.TextPrimary)
        }
    }
}

@Composable
private fun ComfortPicksSection() {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Column {
        Text("Comfort Picks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MoonlitColors.TextPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ComfortItem(
                title = "Dark Chocolate", 
                icon = Icons.Outlined.Restaurant,
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("zomato://search?q=dark%20chocolate"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.zomato.com/search?q=dark%20chocolate"))
                        context.startActivity(webIntent)
                    }
                }
            )
            ComfortItem(
                title = "Herbal Tea", 
                icon = Icons.Outlined.LocalFlorist,
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("blinkit://search?q=herbal%20tea"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://blinkit.com/s/?q=herbal%20tea"))
                        context.startActivity(webIntent)
                    }
                }
            )
        }
    }
}

@Composable
private fun ComfortItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MoonlitColors.Surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MoonlitColors.SecondaryAccent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = MoonlitColors.TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ReflectionSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MoonlitColors.MoodAccent.copy(alpha = 0.2f))
            .padding(20.dp)
    ) {
        Column {
            Text("Today's Reflection", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MoonlitColors.TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You've tracked your well-being consistently this week. Your energy levels seem slightly higher today.",
                fontSize = 15.sp,
                color = MoonlitColors.TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}
