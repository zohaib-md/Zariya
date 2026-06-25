package com.project.zariya.feature.profile.presentation.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.Result
import com.project.zariya.feature.profile.domain.model.ProfileType
import com.project.zariya.feature.profile.domain.model.UserProfile
import com.project.zariya.feature.profile.domain.usecase.CreateProfileUseCase
import com.project.zariya.feature.profile.domain.usecase.GetProfileByIdUseCase
import com.project.zariya.feature.profile.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditProfileUiState(
    val name: String = "",
    val type: ProfileType = ProfileType.SELF,
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    val medicalConditions: String = "",
    val allergies: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditProfileViewModel @Inject constructor(
    private val createProfileUseCase: CreateProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileId: String? = savedStateHandle["id"]
    private var currentProfile: UserProfile? = null

    private val _uiState = MutableStateFlow(AddEditProfileUiState())
    val uiState: StateFlow<AddEditProfileUiState> = _uiState.asStateFlow()

    init {
        profileId?.let { id ->
            if (id.isNotBlank()) {
                loadProfile(id)
            }
        }
    }

    private fun loadProfile(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = getProfileByIdUseCase(id)
            if (profile != null) {
                currentProfile = profile
                _uiState.update {
                    it.copy(
                        name = profile.name,
                        type = profile.profileType,
                        age = profile.age?.toString() ?: "",
                        height = profile.height?.toString() ?: "",
                        weight = profile.weight?.toString() ?: "",
                        medicalConditions = profile.medicalConditions,
                        allergies = profile.allergies,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Profile not found") }
            }
        }
    }

    fun updateState(newState: AddEditProfileUiState) {
        _uiState.value = newState
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name cannot be empty") }
            return
        }

        val parsedAge = state.age.toIntOrNull()
        val parsedHeight = state.height.toIntOrNull()
        val parsedWeight = state.weight.toFloatOrNull()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            if (currentProfile != null) {
                // Update existing
                val updatedProfile = currentProfile!!.copy(
                    name = state.name.trim(),
                    profileType = state.type,
                    age = parsedAge,
                    height = parsedHeight,
                    weight = parsedWeight,
                    medicalConditions = state.medicalConditions.trim(),
                    allergies = state.allergies.trim()
                )
                updateProfileUseCase(updatedProfile)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } else {
                // Create new
                val result = createProfileUseCase(
                    name = state.name,
                    type = state.type,
                    age = parsedAge,
                    height = parsedHeight,
                    weight = parsedWeight,
                    medicalConditions = state.medicalConditions.trim(),
                    allergies = state.allergies.trim()
                )
                
                when (result) {
                    is Result.Success -> _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                    is Result.Loading -> {}
                }
            }
        }
    }
}
