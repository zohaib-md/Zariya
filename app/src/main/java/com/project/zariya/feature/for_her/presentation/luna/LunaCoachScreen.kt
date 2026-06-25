package com.project.zariya.feature.for_her.presentation.luna

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.zariya.feature.for_her.presentation.theme.MoonlitColors

import androidx.hilt.navigation.compose.hiltViewModel

data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunaCoachScreen(
    onNavigateBack: () -> Unit,
    viewModel: LunaCoachViewModel = hiltViewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MoonlitColors.PrimaryAccent), contentAlignment = Alignment.Center) {
                            Text("🌙", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Luna", color = MoonlitColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MoonlitColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MoonlitColors.Surface)
            )
        },
        containerColor = MoonlitColors.BackgroundStart
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            CircularProgressIndicator(color = MoonlitColors.PrimaryAccent, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Input area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MoonlitColors.BackgroundStart)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Share how you feel...", color = MoonlitColors.TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MoonlitColors.BackgroundStart,
                        unfocusedContainerColor = MoonlitColors.BackgroundStart,
                        focusedBorderColor = MoonlitColors.PrimaryAccent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    val userText = inputText
                                    inputText = ""
                                    viewModel.sendMessage(userText)
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = if (isLoading) MoonlitColors.TextSecondary else MoonlitColors.TextPrimary)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(if (isUser) MoonlitColors.PrimaryAccent else MoonlitColors.Surface)
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                color = MoonlitColors.TextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}
