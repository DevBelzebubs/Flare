package com.social.flare.features.admin.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.admin.domain.model.AdminDashboardData
import com.social.flare.features.admin.domain.model.AdminPost
import com.social.flare.features.admin.domain.model.AdminUser
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.admin.domain.repository.AdminRepository
import com.social.flare.features.admin.domain.usecase.CreateAiProfileUseCase
import com.social.flare.features.ai.domain.model.AiPersona
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
    val bots: List<AiPersona> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val actionLoading: Set<String> = emptySet()
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
                val bots = adminRepository.getAllBots()
                _uiState.update { it.copy(dashboard = data, bots = bots, isLoading = false) }
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
            _uiState.update { it.copy(actionLoading = it.actionLoading + "status:$citizenId") }
            try {
                adminRepository.updateUserStatus(citizenId, status)
                loadUsers()
                _uiState.update { it.copy(actionLoading = it.actionLoading - "status:$citizenId", successMessage = "Estado actualizado a $status") }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionLoading = it.actionLoading - "status:$citizenId", errorMessage = e.message) }
            }
        }
    }

    fun deleteUser(citizenId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = it.actionLoading + "deleteuser:$citizenId") }
            try {
                adminRepository.deleteUser(citizenId)
                loadUsers()
                _uiState.update { it.copy(actionLoading = it.actionLoading - "deleteuser:$citizenId", successMessage = "Usuario eliminado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionLoading = it.actionLoading - "deleteuser:$citizenId", errorMessage = e.message) }
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
            _uiState.update { it.copy(actionLoading = it.actionLoading + "deletepost:$postId") }
            try {
                adminRepository.deletePost(postId)
                loadPosts()
                _uiState.update { it.copy(actionLoading = it.actionLoading - "deletepost:$postId", successMessage = "Publicación eliminada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionLoading = it.actionLoading - "deletepost:$postId", errorMessage = e.message) }
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

    fun createNews(title: String, description: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = adminRepository.createNews(title, description, imageUri)

            if (result.isSuccess) {
                loadNews()
                _uiState.update { it.copy(successMessage = "Noticia creada exitosamente", isLoading = false) }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message, isLoading = false) }
            }
        }
    }

    fun updateNews(newsId: String, title: String, description: String, imageUri: Uri?, currentImageUrl: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = adminRepository.updateNews(newsId, title, description, imageUri, currentImageUrl)

            if (result.isSuccess) {
                loadNews()
                _uiState.update { it.copy(successMessage = "Noticia actualizada", isLoading = false) }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message, isLoading = false) }
            }
        }
    }

    fun toggleNewsActive(newsId: String, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = it.actionLoading + "togglenews:$newsId") }
            try {
                adminRepository.toggleNewsActive(newsId, isActive)
                loadNews()
                _uiState.update { it.copy(actionLoading = it.actionLoading - "togglenews:$newsId", successMessage = if (isActive) "Noticia activada" else "Noticia desactivada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionLoading = it.actionLoading - "togglenews:$newsId", errorMessage = e.message) }
            }
        }
    }

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = it.actionLoading + "deletenews:$newsId") }
            try {
                adminRepository.deleteNews(newsId)
                loadNews()
                _uiState.update { it.copy(actionLoading = it.actionLoading - "deletenews:$newsId", successMessage = "Noticia eliminada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(actionLoading = it.actionLoading - "deletenews:$newsId", errorMessage = e.message) }
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

    fun toggleBotStatus(citizenId: String, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = it.actionLoading + "bot:$citizenId") }
            try {
                _uiState.update { currentState ->
                    val updatedBots = currentState.bots.map { bot ->
                        if (bot.citizenId == citizenId) bot.copy(isActive = isActive) else bot
                    }
                    currentState.copy(bots = updatedBots)
                }

                val result = adminRepository.toggleBotStatus(citizenId, isActive)

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(actionLoading = it.actionLoading - "bot:$citizenId", successMessage = if (isActive) "Bot activado" else "Bot desactivado")
                    }
                } else {
                    _uiState.update { currentState ->
                        val revertedBots = currentState.bots.map { bot ->
                            if (bot.citizenId == citizenId) bot.copy(isActive = !isActive) else bot
                        }
                        currentState.copy(
                            bots = revertedBots,
                            actionLoading = currentState.actionLoading - "bot:$citizenId",
                            errorMessage = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    val revertedBots = currentState.bots.map { bot ->
                        if (bot.citizenId == citizenId) bot.copy(isActive = !isActive) else bot
                    }
                    currentState.copy(bots = revertedBots, actionLoading = currentState.actionLoading - "bot:$citizenId", errorMessage = e.message)
                }
            }
        }
    }
    fun updateAiProfile(citizenId: String, username: String, displayName: String, prompt: String, temp: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = it.actionLoading + "editbot:$citizenId") }
            try {
                val result = adminRepository.updateAiPersona(citizenId, displayName, username, prompt, temp)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            actionLoading = it.actionLoading - "editbot:$citizenId",
                            successMessage = "Agente IA '$username' actualizado"
                        )
                    }
                    val updatedBots = adminRepository.getAllBots()
                    _uiState.update { it.copy(bots = updatedBots) }
                } else {
                    _uiState.update {
                        it.copy(
                            actionLoading = it.actionLoading - "editbot:$citizenId",
                            errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionLoading = it.actionLoading - "editbot:$citizenId",
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun deleteBot(citizenId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = it.actionLoading + "deletebot:$citizenId") }
            try {
                val result = adminRepository.deleteBot(citizenId)
                if (result.isSuccess) {
                    val updatedBots = adminRepository.getAllBots()
                    _uiState.update {
                        it.copy(
                            bots = updatedBots,
                            actionLoading = it.actionLoading - "deletebot:$citizenId",
                            successMessage = "Bot eliminado"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            actionLoading = it.actionLoading - "deletebot:$citizenId",
                            errorMessage = result.exceptionOrNull()?.message ?: "Error al eliminar bot"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionLoading = it.actionLoading - "deletebot:$citizenId",
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
    }
}