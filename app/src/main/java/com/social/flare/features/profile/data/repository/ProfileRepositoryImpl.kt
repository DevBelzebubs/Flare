package com.social.flare.features.profile.data.repository

import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.profile.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProfileRepositoryImpl(
    private val citizenDao: CitizenDao,
    private val supabase: SupabaseClient
) : ProfileRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun getCitizenProfile(citizenId: String): Flow<CitizenEntity?> {
        scope.launch {
            try {
                val remote = supabase.postgrest["citizens"]
                    .select { filter { eq("citizen_id", citizenId) } }
                    .decodeSingle<CitizenEntity>()
                citizenDao.insertCitizen(remote)
            } catch (e: Throwable) { e.printStackTrace() }
        }
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
            supabase.postgrest["citizens"].update({
                set("display_name", displayName)
                set("bio", bio ?: "")
                set("avatar_url", avatarUrl ?: "")
                set("banner_url", bannerUrl ?: "")
            }) {
                filter { eq("citizen_id", citizenId) }
            }
            citizenDao.updateCitizenProfile(citizenId, displayName, bio, avatarUrl, bannerUrl)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}