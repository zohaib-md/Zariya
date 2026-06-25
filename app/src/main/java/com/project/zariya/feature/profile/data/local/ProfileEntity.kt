package com.project.zariya.feature.profile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val profileType: String,
    val avatarUri: String?,
    val age: Int?,
    val height: Int?,
    val weight: Float?,
    val medicalConditions: String,
    val allergies: String,
    val createdAt: Long,
    val isActive: Boolean
)
