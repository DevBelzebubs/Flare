package com.social.flare.features.search.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class TrendingHashtag(
    val name: String,
    val postCount: Int
)
