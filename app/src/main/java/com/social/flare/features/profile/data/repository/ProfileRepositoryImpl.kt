package com.social.flare.features.profile.data.repository

import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl(
    private val citizenDao: CitizenDao
) : ProfileRepository {
    override suspend fun getCitizenProfile(citizenId: String): Flow<CitizenEntity?> {
        return citizenDao.observeCitizenById(citizenId)
    }

    override suspend fun updateProfile(
        citizenId: String,
        displayName: String,
        bio: String?,
        avatarUrl: String?,
        bannerUrl: String?
    ): Result<Unit> {
        return try {
            citizenDao.updateCitizenProfile(citizenId, displayName, bio, avatarUrl, bannerUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}