package com.social.flare.features.notifications.domain.usecase

import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.profile.data.local.dao.FollowDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class GetSuggestedAccountsUseCase(
    private val citizenDao: CitizenDao,
    private val followDao: FollowDao,
    private val supabase: SupabaseClient
) {
    suspend operator fun invoke(currentUserId: String): List<CitizenEntity> {
        try {
            val citizens = supabase.postgrest["citizens"]
                .select()
                .decodeList<CitizenEntity>()
            citizens.forEach { citizenDao.insertCitizen(it) }
        } catch (_: Exception) {}
        val followedIds = followDao.getFollowedIds(currentUserId)
        return citizenDao.getAllCitizens()
            .filter { it.citizen_id != currentUserId && it.citizen_id !in followedIds }
            .shuffled()
            .take(10)
    }
}