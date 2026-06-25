package com.project.zariya.feature.profile.data.repository

import com.project.zariya.feature.profile.data.local.ProfileDao
import com.project.zariya.feature.profile.data.mapper.ProfileMapper.toDomain
import com.project.zariya.feature.profile.data.mapper.ProfileMapper.toEntity
import com.project.zariya.feature.profile.domain.model.UserProfile
import com.project.zariya.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<UserProfile>> {
        return profileDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveProfile(): Flow<UserProfile?> {
        return profileDao.getActive().map { it?.toDomain() }
    }

    override suspend fun getProfileById(id: String): UserProfile? {
        return profileDao.getById(id)?.toDomain()
    }

    override suspend fun createProfile(profile: UserProfile) {
        if (profile.isActive) {
            profileDao.deactivateAll()
        }
        profileDao.insert(profile.toEntity())
    }

    override suspend fun updateProfile(profile: UserProfile) {
        if (profile.isActive) {
            profileDao.deactivateAll()
        }
        profileDao.update(profile.toEntity())
    }

    override suspend fun deleteProfile(id: String) {
        profileDao.delete(id)
    }

    override suspend fun switchActiveProfile(id: String) {
        profileDao.deactivateAll()
        profileDao.setActive(id)
    }
}
