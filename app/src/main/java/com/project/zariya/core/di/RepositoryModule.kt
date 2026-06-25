package com.project.zariya.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.project.zariya.feature.profile.domain.repository.ProfileRepository
import com.project.zariya.feature.profile.data.repository.ProfileRepositoryImpl
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import com.project.zariya.feature.medicine.data.repository.MedicineRepositoryImpl
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.data.repository.ReminderRepositoryImpl
import com.project.zariya.feature.reminder.domain.scheduler.AlarmScheduler
import com.project.zariya.feature.reminder.data.scheduler.AlarmSchedulerImpl
import com.project.zariya.feature.refill.domain.repository.RefillRepository
import com.project.zariya.feature.refill.data.repository.RefillRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    abstract fun bindMedicineRepository(
        medicineRepositoryImpl: MedicineRepositoryImpl
    ): MedicineRepository

    @Binds
    abstract fun bindReminderRepository(
        reminderRepositoryImpl: ReminderRepositoryImpl
    ): ReminderRepository

    @Binds
    abstract fun bindAlarmScheduler(
        alarmSchedulerImpl: AlarmSchedulerImpl
    ): AlarmScheduler

    @Binds
    abstract fun bindRefillRepository(
        refillRepositoryImpl: RefillRepositoryImpl
    ): RefillRepository
}
