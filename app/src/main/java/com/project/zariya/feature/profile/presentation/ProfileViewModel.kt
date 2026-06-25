package com.project.zariya.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.Result
import com.project.zariya.feature.profile.domain.model.ProfileType
import com.project.zariya.feature.profile.domain.model.UserProfile
import com.project.zariya.feature.profile.domain.usecase.CreateProfileUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import com.project.zariya.feature.profile.domain.usecase.GetProfilesUseCase
import com.project.zariya.feature.profile.domain.usecase.SwitchProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profiles: List<UserProfile> = emptyList(),
    val activeProfile: UserProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val switchProfileUseCase: SwitchProfileUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        getProfilesUseCase(),
        getActiveProfileUseCase(),
        _error
    ) { profiles, activeProfile, error ->
        ProfileUiState(
            profiles = profiles,
            activeProfile = activeProfile,
            isLoading = false,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(isLoading = true)
    )

    fun createProfile(name: String, type: ProfileType) {
        viewModelScope.launch {
            val isInitial = uiState.value.profiles.isEmpty()
            when (val result = createProfileUseCase(name, type, isInitial = isInitial)) {
                is Result.Success -> _error.value = null
                is Result.Error -> _error.value = result.message
                is Result.Loading -> {}
            }
        }
    }

    fun switchProfile(id: String) {
        viewModelScope.launch {
            switchProfileUseCase(id)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
