package com.project.zariya.feature.for_her.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "cycle_logs")
data class CycleLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val startDateMillis: Long,
    val endDateMillis: Long? = null,
    val flowIntensity: String? = null // e.g. "Light", "Medium", "Heavy"
)
