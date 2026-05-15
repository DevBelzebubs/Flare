package com.social.flare.features.profile.presentation

import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.social.flare.FlareApp
import com.social.flare.features.feed.data.repository.FeedRepositoryImpl
import com.social.flare.features.post.domain.usecase.GetUserPostsUseCase
import com.social.flare.features.profile.data.repository.ProfileRepositoryImpl
import com.social.flare.features.profile.presentation.viewmodel.ProfileViewModel

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val app = context.applicationContext as FlareApp
            val database = app.database
            val profileRepository = ProfileRepositoryImpl(database.citizenDao())
            val feedRepository = FeedRepositoryImpl(database.postDao())
            val getPostsUseCase = GetUserPostsUseCase(feedRepository)
            return ProfileViewModel(
                repository = profileRepository,
                getUserPostsUseCase = getPostsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}