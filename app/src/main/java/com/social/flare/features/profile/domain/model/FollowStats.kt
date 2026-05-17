package com.social.flare.features.profile.domain.model

data class FollowStats(
    val followersCount: Int,
    val followingCount: Int,
    val isFollowingByMe: Boolean
)
