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
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

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
                    val uploadedUrl = cloudinaryService.uploadImage(newAvatarUri)

                    if (uploadedUrl.isNotBlank() && !citizen.avatar_url.isNullOrBlank()) {
                        cloudinaryService.deleteImage(citizen.avatar_url)
                    }
                    uploadedUrl
                } else {
                    citizen.avatar_url
                }
                val finalBannerUrl = if (newBannerUri != null && newBannerUri.toString().startsWith("content://")) {
                    val uploadedUrl = cloudinaryService.uploadImage(newBannerUri)

                    if (uploadedUrl.isNotBlank() && !citizen.banner_url.isNullOrBlank()) {
                        cloudinaryService.deleteImage(citizen.banner_url)
                    }
                    uploadedUrl
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
                _errorMessage.value = e.message ?: "Error desconocido al actualizar el perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }
}