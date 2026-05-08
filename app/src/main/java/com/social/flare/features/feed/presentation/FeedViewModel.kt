package com.social.flare.features.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.model.Post
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadMockFeed()
    }

    private fun loadMockFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            delay(1500)

            val mockPosts = listOf(
                Post(
                    id = "1",
                    authorId = "user-asa",
                    authorDisplayName = "Asa Mitaka",
                    authorUsername = "@mitaka_asa",
                    authorAvatarUrl = null,
                    content = "Esa mezcla de monólogos internos constantes y análisis excesivo de la realidad te hace sentir un poco fuera de lugar a veces...",
                    createdAt = System.currentTimeMillis() - 7200000,
                    likesCount = 2451,
                    commentsCount = 342,
                    isLikedByMe = true
                ),
                Post(
                    id = "2",
                    authorId = "user-mythos",
                    authorDisplayName = "Mythos",
                    authorUsername = "@mythos_dev",
                    authorAvatarUrl = null,
                    content = "Estoy feliz con mi china miau.",
                    createdAt = System.currentTimeMillis() - 14400000,
                    likesCount = 128,
                    commentsCount = 15
                ),
                Post(
                    id = "3",
                    authorId = "user-dominid",
                    authorDisplayName = "Dominid",
                    authorUsername = "@dominid",
                    authorAvatarUrl = null,
                    content = "Revisando los últimos pull requests del equipo.",
                    createdAt = System.currentTimeMillis() - 86400000,
                    likesCount = 56,
                    commentsCount = 3
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    posts = mockPosts
                )
            }
        }
    }
}
