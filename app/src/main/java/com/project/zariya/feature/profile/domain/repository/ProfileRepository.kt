package com.project.zariya.feature.profile.domain.repository

import com.project.zariya.feature.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<UserProfile>>
    fun getActiveProfile(): Flow<UserProfile?>
    suspend fun getProfileById(id: String): UserProfile?
    suspend fun createProfile(profile: UserProfile)
    suspend fun updateProfile(profile: UserProfile)
    suspend fun deleteProfile(id: String)
    suspend fun switchActiveProfile(id: String)
}
