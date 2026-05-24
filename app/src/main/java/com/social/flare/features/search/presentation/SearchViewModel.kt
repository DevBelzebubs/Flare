package com.social.flare.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.admin.domain.repository.AdminRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val news: List<NewsItem> = emptyList(),
    val isLoading: Boolean = false
)

class SearchViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var newsJob: Job? = null

    fun loadNews() {
        newsJob?.cancel()
        newsJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            adminRepository.getActiveNews().collect { newsList ->
                _uiState.value = SearchUiState(news = newsList, isLoading = false)
            }
        }
    }
}
