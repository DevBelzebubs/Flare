package com.social.flare.features.admin.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class AdminDashboardData(
    val totalUsers: Int,
    val activeUsers: Int,
    val blockedUsers: Int,
    val totalPosts: Int,
    val totalNews: Int
)

@Immutable
data class AdminUser(
    val citizenId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val isAdmin: Boolean,
    val status: String,
    val postsCount: Int
)

@Immutable
data class AdminPost(
    val postId: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val content: String?,
    val mediaUrls: List<String>,
    val createdAt: Long,
    val likesCount: Int,
    val commentsCount: Int
)

@Immutable
data class NewsItem(
    val newsId: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val createdAt: Long,
    val isActive: Boolean
)
