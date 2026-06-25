package com.project.zariya.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.project.zariya.feature.profile.data.local.ProfileEntity
import com.project.zariya.feature.profile.data.local.ProfileDao
import com.project.zariya.feature.medicine.data.local.MedicineEntity
import com.project.zariya.feature.medicine.data.local.MedicineDao
import com.project.zariya.feature.reminder.data.local.ReminderEntity
import com.project.zariya.feature.reminder.data.local.ReminderDao
import com.project.zariya.feature.reminder.data.local.DoseLogEntity
import com.project.zariya.feature.reminder.data.local.DoseLogDao
import com.project.zariya.feature.refill.data.local.RefillInfoEntity
import com.project.zariya.feature.refill.data.local.RefillDao
import com.project.zariya.feature.for_her.data.local.CycleLogEntity
import com.project.zariya.feature.for_her.data.local.WellbeingLogEntity
import com.project.zariya.feature.for_her.data.local.ForHerDao

@Database(
    entities = [
        ProfileEntity::class,
        MedicineEntity::class,
        ReminderEntity::class,
        DoseLogEntity::class,
        RefillInfoEntity::class,
        CycleLogEntity::class,
        WellbeingLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ZariyaDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun medicineDao(): MedicineDao
    abstract fun reminderDao(): ReminderDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun refillDao(): RefillDao
    abstract fun forHerDao(): ForHerDao
}
