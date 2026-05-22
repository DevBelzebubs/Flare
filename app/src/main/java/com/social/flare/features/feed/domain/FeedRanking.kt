package com.social.flare.features.feed.domain

object FeedRanking {
    fun score(
        likesCount: Int,
        commentsCount: Int,
        createdAt: Long,
        currentTime: Long = System.currentTimeMillis(),
        isFollowed: Boolean = false
    ): Double {
        val engagement = likesCount + commentsCount * 2.0
        val ageHours = (currentTime - createdAt) / 3600000.0
        val boost = if (isFollowed) 2.0 else 1.0
        return boost * (1.0 + engagement) / (1.0 + ageHours / 24.0)
    }
}
