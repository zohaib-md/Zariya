package com.project.zariya.feature.profile.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val profileType: ProfileType,
    val avatarUri: String?,
    val age: Int?,
    val height: Int?,
    val weight: Float?,
    val medicalConditions: String,
    val allergies: String,
    val createdAt: Long,
    val isActive: Boolean
)
