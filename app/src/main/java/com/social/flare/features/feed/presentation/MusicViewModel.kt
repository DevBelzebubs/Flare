package com.social.flare.features.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.feed.domain.model.FlareTrack
import com.social.flare.features.feed.domain.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<FlareTrack>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            delay(500)

            val result = musicRepository.searchMusic(query)
            if (result.isSuccess) {
                _searchResults.value = result.getOrDefault(emptyList())
            }
            _isLoading.value = false
        }
    }
}