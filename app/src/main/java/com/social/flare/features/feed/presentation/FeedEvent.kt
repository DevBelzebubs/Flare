package com.social.flare.features.feed.presentation

sealed interface FeedEvent {
    object OnRefresh : FeedEvent
    data class OnLikeClick(val postId: String) : FeedEvent
    data class OnSaveClick(val postId: String) : FeedEvent
    data class OnShareClick(val postId: String) : FeedEvent
    data class OnCommentClick(val postId: String) : FeedEvent
    //--------------------------------------------------------
    data class OnDeletePost(val postId: String) : FeedEvent
    data class OnEditPost(val postId: String, val newContent: String) : FeedEvent
    data class OnPostClick(val postId: String) : FeedEvent
}