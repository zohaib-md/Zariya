package com.project.zariya.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.feature.auth.domain.model.AuthUser
import com.project.zariya.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: AuthUser? = null,
    val error: String? = null,
    // Login form
    val loginEmail: String = "",
    val loginPassword: String = "",
    // Sign up form
    val signUpName: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val signUpConfirmPassword: String = "",
    // Forgot password
    val resetEmail: String = "",
    val resetEmailSent: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check current auth state
        _uiState.update {
            it.copy(
                isAuthenticated = authRepository.isLoggedIn,
                user = authRepository.currentUser
            )
        }

        // Observe auth state changes
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _uiState.update {
                    it.copy(
                        isAuthenticated = user != null,
                        user = user
                    )
                }
            }
        }
    }

    // --- Login form ---
    fun onLoginEmailChange(email: String) {
        _uiState.update { it.copy(loginEmail = email, error = null) }
    }

    fun onLoginPasswordChange(password: String) {
        _uiState.update { it.copy(loginPassword = password, error = null) }
    }

    fun signInWithEmail() {
        val state = _uiState.value
        if (state.loginEmail.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email") }
            return
        }
        if (state.loginPassword.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your password") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signInWithEmail(state.loginEmail.trim(), state.loginPassword)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true, user = user) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    // --- Sign up form ---
    fun onSignUpNameChange(name: String) {
        _uiState.update { it.copy(signUpName = name, error = null) }
    }

    fun onSignUpEmailChange(email: String) {
        _uiState.update { it.copy(signUpEmail = email, error = null) }
    }

    fun onSignUpPasswordChange(password: String) {
        _uiState.update { it.copy(signUpPassword = password, error = null) }
    }

    fun onSignUpConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(signUpConfirmPassword = password, error = null) }
    }

    fun signUpWithEmail() {
        val state = _uiState.value
        if (state.signUpName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your name") }
            return
        }
        if (state.signUpEmail.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email") }
            return
        }
        if (state.signUpPassword.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        if (state.signUpPassword != state.signUpConfirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signUpWithEmail(
                state.signUpEmail.trim(),
                state.signUpPassword,
                state.signUpName.trim()
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true, user = user) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    // --- Google Sign In ---
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true, user = user) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    // --- Forgot Password ---
    fun onResetEmailChange(email: String) {
        _uiState.update { it.copy(resetEmail = email, error = null) }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.resetEmail
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.sendPasswordResetEmail(email.trim())
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, resetEmailSent = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    // --- Sign Out ---
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { AuthUiState() }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
