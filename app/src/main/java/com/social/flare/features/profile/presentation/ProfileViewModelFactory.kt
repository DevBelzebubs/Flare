package com.social.flare.features.profile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.social.flare.FlareApp
import com.social.flare.features.feed.data.repository.FeedRepositoryImpl
import com.social.flare.features.post.domain.usecase.GetUserPostsUseCase
import com.social.flare.features.profile.data.repository.FollowRepositoryImpl
import com.social.flare.features.profile.data.repository.ProfileRepositoryImpl
import com.social.flare.features.profile.domain.usecase.GetFollowStatsUseCase
import com.social.flare.features.profile.domain.usecase.ToggleFollowUseCase
import com.social.flare.features.profile.presentation.viewmodel.ProfileViewModel

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val app = context.applicationContext as FlareApp
            val database = app.database

            val profileRepository = ProfileRepositoryImpl(database.citizenDao())
            val feedRepository = FeedRepositoryImpl(database.postDao())
            val followRepository = FollowRepositoryImpl(database.followDao())

            val getPostsUseCase = GetUserPostsUseCase(feedRepository)
            val toggleFollowUseCase = ToggleFollowUseCase(followRepository)
            val getFollowStatsUseCase = GetFollowStatsUseCase(followRepository)

            return ProfileViewModel(
                repository = profileRepository,
                getUserPostsUseCase = getPostsUseCase,
                postDao = database.postDao(),
                toggleFollowUseCase = toggleFollowUseCase,
                getFollowStatsUseCase = getFollowStatsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}