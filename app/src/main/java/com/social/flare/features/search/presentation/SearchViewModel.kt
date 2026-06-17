package com.social.flare.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.admin.domain.repository.AdminRepository
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.search.domain.model.TrendingHashtag
import com.social.flare.features.search.domain.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val searchResults: SearchResults? = null,
    val selectedTab: SearchTab = SearchTab.PROFILES,
    val explorePosts: List<Post> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val trendingHashtags: List<TrendingHashtag> = emptyList(),
    val isLoading: Boolean = false
)
enum class SearchTab { PROFILES, POSTS, HASHTAGS }
data class SearchResults(
    val profiles: List<CitizenEntity>,
    val posts: List<Post>,
    val hashtagPosts: List<Post>
)


class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val adminRepository: AdminRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var newsJob: Job? = null
    private var exploreJob: Job? = null
    private var trendingJob: Job? = null

    init {
        loadNews()
        loadExplorePosts()
        loadTrendingHashtags()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = null) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            search(query)
        }
    }

    fun selectTab(tab: SearchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        val query = _uiState.value.query
        if (query.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch { search(query) }
        }
    }

    fun onHashtagClick(hashtag: String) {
        _uiState.update { it.copy(query = hashtag, selectedTab = SearchTab.HASHTAGS) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch { search(hashtag) }
    }

    private suspend fun search(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        val profiles = searchRepository.searchUsers(query).first()
        val posts = searchRepository.searchPosts(query, currentUserId).first()
        val hashtagPosts = searchRepository.searchHashtagPosts(query, currentUserId).first()
        _uiState.update { it.copy(
            searchResults = SearchResults(
                profiles = profiles,
                posts = posts,
                hashtagPosts = hashtagPosts
            ),
            isLoading = false
        ) }
    }

    fun loadNews() {
        newsJob?.cancel()
        newsJob = viewModelScope.launch {
            adminRepository.getActiveNews().collect { newsList ->
                _uiState.update { it.copy(news = newsList, isLoading = false) }
            }
        }
    }

    fun loadExplorePosts() {
        exploreJob?.cancel()
        exploreJob = viewModelScope.launch {
            searchRepository.getExplorePosts(currentUserId).collect { posts ->
                _uiState.update { it.copy(explorePosts = posts) }
            }
        }
    }

    fun loadTrendingHashtags() {
        trendingJob?.cancel()
        trendingJob = viewModelScope.launch {
            searchRepository.getTrendingHashtags().collect { hashtags ->
                _uiState.update { it.copy(trendingHashtags = hashtags) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        newsJob?.cancel()
        exploreJob?.cancel()
        trendingJob?.cancel()
    }
}
