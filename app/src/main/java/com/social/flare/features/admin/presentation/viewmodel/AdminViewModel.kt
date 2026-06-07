package com.social.flare.features.admin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.admin.domain.model.AdminDashboardData
import com.social.flare.features.admin.domain.model.AdminPost
import com.social.flare.features.admin.domain.model.AdminUser
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.admin.domain.repository.AdminRepository
import com.social.flare.features.admin.domain.usecase.CreateAiProfileUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class AdminUiState(
    val isLoading: Boolean = false,
    val dashboard: AdminDashboardData? = null,
    val users: List<AdminUser> = emptyList(),
    val posts: List<AdminPost> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val adminRepository: AdminRepository,
    private val createAiProfileUseCase: CreateAiProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadDashboard() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val data = adminRepository.getDashboardData()
                _uiState.update { it.copy(dashboard = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun loadUsers() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val users = adminRepository.getAllUsers()
                _uiState.update { it.copy(users = users, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun updateUserStatus(citizenId: String, status: String) {
        viewModelScope.launch {
            try {
                adminRepository.updateUserStatus(citizenId, status)
                loadUsers()
                _uiState.update { it.copy(successMessage = "Estado actualizado a $status") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deleteUser(citizenId: String) {
        viewModelScope.launch {
            try {
                adminRepository.deleteUser(citizenId)
                loadUsers()
                _uiState.update { it.copy(successMessage = "Usuario eliminado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun loadPosts() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val posts = adminRepository.getAllPosts()
                _uiState.update { it.copy(posts = posts, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                adminRepository.deletePost(postId)
                loadPosts()
                _uiState.update { it.copy(successMessage = "Publicación eliminada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun loadNews() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                adminRepository.getAllNews().collect { newsList ->
                    _uiState.update { it.copy(news = newsList, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun createNews(title: String, description: String, imageUrl: String?) {
        viewModelScope.launch {
            try {
                adminRepository.createNews(title, description, imageUrl)
                loadNews()
                _uiState.update { it.copy(successMessage = "Noticia creada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun updateNews(newsId: String, title: String, description: String, imageUrl: String?) {
        viewModelScope.launch {
            try {
                adminRepository.updateNews(newsId, title, description, imageUrl)
                loadNews()
                _uiState.update { it.copy(successMessage = "Noticia actualizada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun toggleNewsActive(newsId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                adminRepository.toggleNewsActive(newsId, isActive)
                loadNews()
                _uiState.update { it.copy(successMessage = if (isActive) "Noticia activada" else "Noticia desactivada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            try {
                adminRepository.deleteNews(newsId)
                loadNews()
                _uiState.update { it.copy(successMessage = "Noticia eliminada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    // --- NUEVA FUNCIÓN PARA GESTIONAR LA IA ---
    fun createAiProfile(username: String, displayName: String, prompt: String, temp: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = createAiProfileUseCase.execute(username, displayName, prompt, temp)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Agente IA '$username' creado exitosamente"
                    )
                }
                // Recargar el dashboard para actualizar el contador de usuarios totales
                loadDashboard()
            } else {
                val error = result.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error?.message ?: "Error al crear el Agente IA"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}