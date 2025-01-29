package com.github.ParagonVirtuoso.memorias.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.usecase.SearchVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchVideosUseCase: SearchVideosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState

    fun search(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                searchVideosUseCase(query).collect { videos ->
                    _uiState.value = SearchUiState.Success(videos)
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Erro ao buscar vídeos")
            }
        }
    }
}

sealed class SearchUiState {
    object Initial : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val videos: List<Video>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
} 