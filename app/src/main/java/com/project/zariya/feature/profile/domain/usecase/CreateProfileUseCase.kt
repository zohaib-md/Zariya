package com.project.zariya.feature.profile.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.profile.domain.model.ProfileType
import com.project.zariya.feature.profile.domain.model.UserProfile
import com.project.zariya.feature.profile.domain.repository.ProfileRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

class CreateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(
        name: String,
        type: ProfileType,
        avatarUri: String? = null,
        age: Int? = null,
        height: Int? = null,
        weight: Float? = null,
        medicalConditions: String = "",
        allergies: String = "",
        isInitial: Boolean = false
    ): Result<Unit> {
        if (name.isBlank()) {
            return Result.error("Name cannot be blank")
        }

        val activeProfile = repository.getActiveProfile().firstOrNull()
        val shouldBeActive = isInitial || activeProfile == null

        val profile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            profileType = type,
            avatarUri = avatarUri,
            age = age,
            height = height,
            weight = weight,
            medicalConditions = medicalConditions,
            allergies = allergies,
            createdAt = System.currentTimeMillis(),
            isActive = shouldBeActive
        )

        return try {
            repository.createProfile(profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to create profile: ${e.message}")
        }
    }
}
