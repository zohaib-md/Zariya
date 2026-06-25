package com.project.zariya.feature.profile.domain.usecase

import com.project.zariya.feature.profile.domain.model.UserProfile
import com.project.zariya.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfilesUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<List<UserProfile>> {
        return repository.getAllProfiles()
    }
}

class GetActiveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<UserProfile?> {
        return repository.getActiveProfile()
    }
}

class SwitchProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(id: String) {
        repository.switchActiveProfile(id)
    }
}

class GetProfileByIdUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(id: String): UserProfile? {
        return repository.getProfileById(id)
    }
}

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) {
        repository.updateProfile(profile)
    }
}
