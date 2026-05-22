package com.social.flare.features.admin.data.repository

import com.social.flare.features.admin.data.local.dao.NewsDao
import com.social.flare.features.admin.data.local.entity.NewsItemEntity
import com.social.flare.features.admin.domain.model.AdminDashboardData
import com.social.flare.features.admin.domain.model.AdminPost
import com.social.flare.features.admin.domain.model.AdminUser
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.admin.domain.repository.AdminRepository
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.feed.data.local.dao.PostDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AdminRepositoryImpl(
    private val citizenDao: CitizenDao,
    private val postDao: PostDao,
    private val newsDao: NewsDao
) : AdminRepository {

    override suspend fun getDashboardData(): AdminDashboardData {
        val allUsers = citizenDao.getAllCitizens()
        val totalPosts = postDao.getAllPostsAdmin()
        val totalNews = newsDao.countNews()

        return AdminDashboardData(
            totalUsers = allUsers.size,
            activeUsers = allUsers.count { it.status == "active" },
            blockedUsers = allUsers.count { it.status != "active" },
            totalPosts = totalPosts.size,
            totalNews = totalNews
        )
    }

    override suspend fun getAllUsers(): List<AdminUser> {
        val citizens = citizenDao.getAllCitizens()
        val result = mutableListOf<AdminUser>()
        for (citizen in citizens) {
            val postsCount = postDao.countPostsByAuthor(citizen.citizen_id)
            result.add(
                AdminUser(
                    citizenId = citizen.citizen_id,
                    username = citizen.username,
                    displayName = citizen.display_name,
                    avatarUrl = citizen.avatar_url,
                    bio = citizen.bio,
                    isAdmin = citizen.is_admin,
                    status = citizen.status,
                    postsCount = postsCount
                )
            )
        }
        return result
    }

    override suspend fun updateUserStatus(citizenId: String, status: String) {
        citizenDao.updateUserStatus(citizenId, status)
    }

    override suspend fun deleteUser(citizenId: String) {
        citizenDao.deleteCitizen(citizenId)
    }

    override suspend fun getAllPosts(): List<AdminPost> {
        return postDao.getAllPostsAdmin().map { pwd ->
            val mediaList = if (pwd.post.media_urls.isNotBlank()) {
                pwd.post.media_urls.split(",")
            } else {
                emptyList()
            }
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

    override suspend fun deletePost(postId: String) {
        postDao.deletePostAdmin(postId)
    }

    override fun getActiveNews(): Flow<List<NewsItem>> {
        return newsDao.getActiveNews().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllNews(): Flow<List<NewsItem>> {
        return newsDao.getAllNews().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createNews(title: String, description: String, imageUrl: String?) {
        val news = NewsItemEntity(
            news_id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            image_url = imageUrl,
            created_at = System.currentTimeMillis(),
            is_active = true
        )
        newsDao.insertNews(news)
    }

    override suspend fun updateNews(newsId: String, title: String, description: String, imageUrl: String?) {
        newsDao.updateNews(newsId, title, description, imageUrl)
    }

    override suspend fun toggleNewsActive(newsId: String, isActive: Boolean) {
        newsDao.toggleNewsActive(newsId, isActive)
    }

    override suspend fun deleteNews(newsId: String) {
        newsDao.deleteNews(newsId)
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
