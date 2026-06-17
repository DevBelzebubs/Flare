package com.social.flare.features.admin.data.repository

import android.net.Uri
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.admin.data.local.dao.NewsDao
import com.social.flare.features.admin.data.local.entity.NewsItemEntity
import com.social.flare.features.admin.data.remote.dto.CreateBotRpcRequest
import com.social.flare.features.admin.domain.model.AdminDashboardData
import com.social.flare.features.admin.domain.model.AdminPost
import com.social.flare.features.admin.domain.model.AdminUser
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.admin.domain.repository.AdminRepository
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.PostEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.collections.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class AiPersonaFetchDto(
    val citizen_id: String,
    val system_prompt: String,
    val temperature: Double,
    val is_active: Boolean? = null,
    val citizens: CitizenMinimalDto? = null
)

@Serializable
private data class CitizenMinimalDto(
    val username: String,
    val display_name: String
)
class AdminRepositoryImpl(
    private val citizenDao: CitizenDao,
    private val postDao: PostDao,
    private val newsDao: NewsDao,
    private val supabase: SupabaseClient,
    private val cloudinaryService: CloudinaryService
) : AdminRepository {

    override suspend fun getDashboardData(): AdminDashboardData = withContext(Dispatchers.IO) {
        syncAllData()
        val allUsers = citizenDao.getAllCitizens()
        val totalPosts = postDao.getAllPostsAdmin()
        val totalNews = newsDao.countNews()

        AdminDashboardData(
            totalUsers = allUsers.size,
            activeUsers = allUsers.count { it.status == "active" },
            blockedUsers = allUsers.count { it.status != "active" },
            totalPosts = totalPosts.size,
            totalNews = totalNews
        )
    }

    override suspend fun createAiPersona(persona: AiPersona): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = CreateBotRpcRequest(
                bot_username = persona.username,
                bot_display_name = persona.displayName,
                bot_system_prompt = persona.systemPrompt,
                bot_temperature = persona.temperature
            )

            supabase.postgrest.rpc("create_ai_bot", request)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): List<AdminUser> = withContext(Dispatchers.IO) {
        syncAllUsers()
        val citizens = citizenDao.getAllCitizens()
        val postCounts = postDao.getPostsCountsByAuthors(citizens.map { it.citizen_id })
        val countsMap = postCounts.associate { it.authorId to it.count }
        citizens.map { citizen ->
            AdminUser(
                citizenId = citizen.citizen_id,
                username = citizen.username,
                displayName = citizen.display_name,
                avatarUrl = citizen.avatar_url,
                bio = citizen.bio,
                isAdmin = citizen.is_admin,
                status = citizen.status,
                postsCount = countsMap[citizen.citizen_id] ?: 0
            )
        }
    }

    override suspend fun updateUserStatus(citizenId: String, status: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["citizens"].update({
                set("status", status)
            }) {
                filter { eq("citizen_id", citizenId) }
            }
        } catch (e: Throwable) { e.printStackTrace() }
        citizenDao.updateUserStatus(citizenId, status)
    }

    override suspend fun deleteUser(citizenId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["citizens"].delete { filter { eq("citizen_id", citizenId) } }
        } catch (e: Throwable) { e.printStackTrace() }
        citizenDao.deleteCitizen(citizenId)
    }

    override suspend fun getAllPosts(): List<AdminPost> = withContext(Dispatchers.IO) {
        syncAllPosts()
        postDao.getAllPostsAdmin().map { pwd ->
            val mediaList = pwd.post.media_urls?.takeIf { it.isNotBlank() }?.split(",") ?: emptyList()
            AdminPost(
                postId = pwd.post.post_id,
                authorId = pwd.post.author_id,
                authorUsername = pwd.authorUsername,
                authorDisplayName = pwd.authorDisplayName,
                content = pwd.post.content,
                mediaUrls = mediaList,
                createdAt = pwd.post.created_at,
                likesCount = pwd.likesCount,
                commentsCount = pwd.commentsCount
            )
        }
    }

    override suspend fun deletePost(postId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["posts"].delete { filter { eq("post_id", postId) } }
        } catch (e: Throwable) { e.printStackTrace() }
        postDao.deletePostAdmin(postId)
    }

    override fun getActiveNews(): Flow<List<NewsItem>> = flow {
        withContext(Dispatchers.IO) { syncAllNews() }
        emitAll(newsDao.getActiveNews().map { entities ->
            entities.map { it.toDomain() }
        })
    }

    override fun getAllNews(): Flow<List<NewsItem>> = flow {
        withContext(Dispatchers.IO) { syncAllNews() }
        emitAll(newsDao.getAllNews().map { entities ->
            entities.map { it.toDomain() }
        })
    }

    override suspend fun createNews(title: String, description: String, imageUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val imageUrl = cloudinaryService.uploadImage(imageUri)

            val news = NewsItemEntity(
                news_id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                image_url = imageUrl,
                created_at = System.currentTimeMillis(),
                is_active = true
            )

            supabase.postgrest["news"].insert(news)
            newsDao.insertNews(news)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error al crear noticia: ${e.message}"))
        }
    }

    override suspend fun updateNews(newsId: String, title: String, description: String, imageUri: Uri?, currentImageUrl: String?): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val finalImageUrl = if (imageUri != null) {
                cloudinaryService.uploadImage(imageUri)
            } else {
                currentImageUrl ?: ""
            }
            supabase.postgrest["news"].update({
                set("title", title)
                set("description", description)
                set("image_url", finalImageUrl)
            }) {
                filter { eq("news_id", newsId) }
            }
            newsDao.updateNews(newsId, title, description, finalImageUrl)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error al actualizar noticia: ${e.message}"))
        }
    }

    override suspend fun toggleNewsActive(newsId: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["news"].update({
                set("is_active", isActive)
            }) {
                filter { eq("news_id", newsId) }
            }
        } catch (e: Throwable) { e.printStackTrace() }
        newsDao.toggleNewsActive(newsId, isActive)
    }

    override suspend fun deleteNews(newsId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["news"].delete { filter { eq("news_id", newsId) } }
        } catch (e: Throwable) { e.printStackTrace() }
        newsDao.deleteNews(newsId)
    }

    override suspend fun getAllBots(): List<AiPersona> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["ai_personas"]
                .select(
                    columns = Columns.raw(
                        "citizen_id, system_prompt, temperature, is_active, citizens(username, display_name)"
                    )
                )
                .decodeList<AiPersonaFetchDto>()
                .map { dto ->
                    AiPersona(
                        citizenId = dto.citizen_id,
                        username = dto.citizens?.username ?: "unknown_bot",
                        displayName = dto.citizens?.display_name ?: "Bot",
                        systemPrompt = dto.system_prompt,
                        temperature = dto.temperature,
                        isActive = dto.is_active ?: true
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun toggleBotStatus(
        citizenId: String,
        isActive: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc(
                "toggle_bot_status",
                buildJsonObject {
                    put("p_citizen_id", citizenId)
                    put("p_is_active", isActive)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBotAvatar(
        citizenId: String,
        avatarUrl: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["citizens"].update({
                set("avatar_url", avatarUrl)
            }) {
                filter { eq("citizen_id", citizenId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncAllData() {
        try {
            syncAllUsers()
            syncAllPosts()
            syncAllNews()
        } catch (e: Throwable) { e.printStackTrace() }
    }

    private suspend fun syncAllUsers() {
        try {
            val users = supabase.postgrest["citizens"]
                .select { limit(500) }
                .decodeList<CitizenEntity>()
            users.forEach { citizenDao.insertCitizen(it) }
        } catch (e: Throwable) { e.printStackTrace() }
    }

    private suspend fun syncAllPosts() {
        try {
            val posts = supabase.postgrest["posts"]
                .select {
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(500)
                }
                .decodeList<PostEntity>()
            val authorIds = posts.map { it.author_id }.distinct()
            val authors = supabase.postgrest["citizens"]
                .select { filter { isIn("citizen_id", authorIds) } }
                .decodeList<CitizenEntity>()
            authors.forEach { citizenDao.insertCitizen(it) }
            posts.forEach { postDao.insertPost(it) }
        } catch (e: Throwable) { e.printStackTrace() }
    }

    private suspend fun syncAllNews() {
        try {
            val news = supabase.postgrest["news"]
                .select {
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(200)
                }
                .decodeList<NewsItemEntity>()
            news.forEach { newsDao.insertNews(it) }
        } catch (e: Throwable) { e.printStackTrace() }
    }

}

private fun NewsItemEntity.toDomain(): NewsItem {
    return NewsItem(
        newsId = this.news_id,
        title = this.title,
        description = this.description,
        imageUrl = this.image_url,
        createdAt = this.created_at,
        isActive = this.is_active
    )
}