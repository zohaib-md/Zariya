package com.project.zariya.feature.for_her.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "wellbeing_logs")
data class WellbeingLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val dateMillis: Long,
    val mood: String? = null, // e.g. "Calm", "Emotional"
    val symptoms: String = "" // Comma-separated list of symptoms
)
