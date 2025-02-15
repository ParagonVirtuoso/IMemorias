package com.github.ParagonVirtuoso.memorias.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.model.SearchParams
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<VideoResult>(VideoResult.Initial)
    val searchResults: StateFlow<VideoResult> = _searchResults

    fun searchVideos(query: String) {
        viewModelScope.launch {
            videoRepository.searchVideos(SearchParams(query))
                .onStart { 
                    _searchResults.value = VideoResult.Loading 
                }
                .catch { exception ->
                    _searchResults.value = VideoResult.Error(exception.message ?: "Erro desconhecido")
                }
                .collect { result ->
                    _searchResults.value = result
                }
        }
    }
} 