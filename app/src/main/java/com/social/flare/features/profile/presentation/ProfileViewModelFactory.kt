package com.social.flare.features.profile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.social.flare.FlareApp
import com.social.flare.features.profile.data.repository.ProfileRepositoryImpl

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val database = (context.applicationContext as FlareApp).database
            val repository = ProfileRepositoryImpl(database.citizenDao())
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}