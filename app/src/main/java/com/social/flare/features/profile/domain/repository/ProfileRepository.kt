package com.social.flare.features.profile.domain.repository

import com.social.flare.features.auth.data.local.entity.CitizenEntity
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getCitizenProfile(citizenId: String): Flow<CitizenEntity?>
    suspend fun updateProfile(citizenId: String, displayName: String, bio: String?, avatarUrl: String?, bannerUrl: String?): Result<Unit>
}