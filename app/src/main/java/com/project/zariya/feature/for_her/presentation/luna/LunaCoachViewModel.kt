package com.project.zariya.feature.for_her.presentation.luna

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.project.zariya.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LunaCoachViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Hi there. I'm Luna \uD83C\uDF19. I'm here to listen, support, and help you understand your body better. How are you feeling today?", false)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("You are Luna, an extremely gentle, empathetic, and knowledgeable AI companion designed for women's wellness. Your tone is warm, supportive, and non-judgmental. You validate the user's feelings and provide practical, comforting advice related to menstrual cycles, well-being, and mood. You keep your responses relatively concise. Do not sound clinical; sound like a wise, caring friend.")
        }
    )

    private val chat = generativeModel.startChat()

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // Add user message to UI
        _messages.update { currentList ->
            currentList + ChatMessage(userText, isUser = true)
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userText)
                response.text?.let { reply ->
                    _messages.update { currentList ->
                        currentList + ChatMessage(reply, isUser = false)
                    }
                }
            } catch (e: Exception) {
                _messages.update { currentList ->
                    currentList + ChatMessage("I'm sorry, I'm having trouble connecting right now. Let's talk again in a moment.", isUser = false)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
