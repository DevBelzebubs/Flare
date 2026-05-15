package com.social.flare.features.profile.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val cloudinaryService: CloudinaryService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun updateProfile(
        citizen: CitizenEntity,
        newName: String,
        newBio: String,
        newAvatarUri: Uri?,
        newBannerUri: Uri?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val finalAvatarUrl = if (newAvatarUri != null && newAvatarUri.toString().startsWith("content://")) {
                    cloudinaryService.uploadImage(newAvatarUri)
                } else {
                    citizen.avatar_url
                }

                val finalBannerUrl = if (newBannerUri != null && newBannerUri.toString().startsWith("content://")) {
                    cloudinaryService.uploadImage(newBannerUri)
                } else {
                    citizen.banner_url
                }

                profileRepository.updateProfile(
                    citizenId = citizen.citizen_id,
                    displayName = newName,
                    bio = newBio.takeIf { it.isNotBlank() },
                    avatarUrl = finalAvatarUrl,
                    bannerUrl = finalBannerUrl
                )

                _isSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}