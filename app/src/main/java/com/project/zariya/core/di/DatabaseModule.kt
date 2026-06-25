package com.project.zariya.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.project.zariya.core.data.database.ZariyaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideZariyaDatabase(
        @ApplicationContext context: Context
    ): ZariyaDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profiles ADD COLUMN age INTEGER")
                db.execSQL("ALTER TABLE profiles ADD COLUMN weight REAL")
                db.execSQL("ALTER TABLE profiles ADD COLUMN medicalConditions TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE profiles ADD COLUMN allergies TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cycle_logs` (" +
                            "`id` TEXT NOT NULL, " +
                            "`profileId` TEXT NOT NULL, " +
                            "`startDateMillis` INTEGER NOT NULL, " +
                            "`endDateMillis` INTEGER, " +
                            "`flowIntensity` TEXT, " +
                            "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wellbeing_logs` (" +
                            "`id` TEXT NOT NULL, " +
                            "`profileId` TEXT NOT NULL, " +
                            "`dateMillis` INTEGER NOT NULL, " +
                            "`mood` TEXT, " +
                            "`symptoms` TEXT NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medicines ADD COLUMN stockCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE medicines ADD COLUMN isStockTracked INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profiles ADD COLUMN height INTEGER")
            }
        }

        return Room.databaseBuilder(
            context,
            ZariyaDatabase::class.java,
            "zariya_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .fallbackToDestructiveMigration(false)
        .build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: ZariyaDatabase) = database.profileDao()

    @Provides
    @Singleton
    fun provideMedicineDao(database: ZariyaDatabase) = database.medicineDao()

    @Provides
    @Singleton
    fun provideReminderDao(database: ZariyaDatabase) = database.reminderDao()

    @Provides
    @Singleton
    fun provideDoseLogDao(database: ZariyaDatabase) = database.doseLogDao()

    @Provides
    @Singleton
    fun provideRefillDao(database: ZariyaDatabase) = database.refillDao()

    @Provides
    @Singleton
    fun provideForHerDao(database: ZariyaDatabase) = database.forHerDao()
}
