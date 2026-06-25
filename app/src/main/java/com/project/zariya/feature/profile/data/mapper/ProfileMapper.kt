package com.project.zariya.feature.profile.data.mapper

import com.project.zariya.feature.profile.data.local.ProfileEntity
import com.project.zariya.feature.profile.domain.model.ProfileType
import com.project.zariya.feature.profile.domain.model.UserProfile

object ProfileMapper {
    fun ProfileEntity.toDomain(): UserProfile {
        return UserProfile(
            id = id,
            name = name,
            profileType = try {
                ProfileType.valueOf(profileType)
            } catch (e: Exception) {
                ProfileType.CUSTOM
            },
            avatarUri = avatarUri,
            age = age,
            height = height,
            weight = weight,
            medicalConditions = medicalConditions,
            allergies = allergies,
            createdAt = createdAt,
            isActive = isActive
        )
    }

    fun UserProfile.toEntity(): ProfileEntity {
        return ProfileEntity(
            id = id,
            name = name,
            profileType = profileType.name,
            avatarUri = avatarUri,
            age = age,
            height = height,
            weight = weight,
            medicalConditions = medicalConditions,
            allergies = allergies,
            createdAt = createdAt,
            isActive = isActive
        )
    }
}
