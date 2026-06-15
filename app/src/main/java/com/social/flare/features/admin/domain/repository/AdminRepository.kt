package com.social.flare.features.admin.domain.repository

import com.social.flare.features.admin.domain.model.AdminDashboardData
import com.social.flare.features.admin.domain.model.AdminPost
import com.social.flare.features.admin.domain.model.AdminUser
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.ai.domain.model.AiPersona
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    suspend fun getDashboardData(): AdminDashboardData
    suspend fun createAiPersona(persona: AiPersona): Result<Unit>
    suspend fun getAllUsers(): List<AdminUser>
    suspend fun updateUserStatus(citizenId: String, status: String)
    suspend fun deleteUser(citizenId: String)

    suspend fun getAllPosts(): List<AdminPost>
    suspend fun deletePost(postId: String)

    fun getActiveNews(): Flow<List<NewsItem>>
    fun getAllNews(): Flow<List<NewsItem>>
    suspend fun createNews(title: String, description: String, imageUrl: String?)
    suspend fun updateNews(newsId: String, title: String, description: String, imageUrl: String?)
    suspend fun toggleNewsActive(newsId: String, isActive: Boolean)
    suspend fun deleteNews(newsId: String)
    suspend fun getAllBots(): List<AiPersona>
    suspend fun toggleBotStatus(citizenId: String, isActive: Boolean): Result<Unit>
    suspend fun updateBotAvatar(citizenId: String, avatarUrl: String): Result<Unit>
}
