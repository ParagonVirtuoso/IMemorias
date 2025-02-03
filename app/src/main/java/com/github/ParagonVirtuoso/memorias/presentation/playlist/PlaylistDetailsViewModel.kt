package com.github.ParagonVirtuoso.memorias.presentation.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.GetPlaylistVideosUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.RemoveVideoFromPlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailsViewModel @Inject constructor(
    private val getPlaylistVideosUseCase: GetPlaylistVideosUseCase,
    private val removeVideoFromPlaylistUseCase: RemoveVideoFromPlaylistUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlist = checkNotNull(savedStateHandle.get<com.github.ParagonVirtuoso.memorias.domain.model.Playlist>("playlist")) {
        "Playlist não encontrada"
    }

    private val _uiState = MutableStateFlow<PlaylistDetailsUiState>(PlaylistDetailsUiState.Loading)
    val uiState: StateFlow<PlaylistDetailsUiState> = _uiState

    init {
        loadPlaylistVideos()
    }

    private fun loadPlaylistVideos() {
        viewModelScope.launch {
            getPlaylistVideosUseCase(playlist.id)
                .onStart { _uiState.value = PlaylistDetailsUiState.Loading }
                .catch { e -> _uiState.value = PlaylistDetailsUiState.Error(e.message ?: "Erro ao carregar vídeos") }
                .collect { videos ->
                    _uiState.value = if (videos.isEmpty()) {
                        PlaylistDetailsUiState.Empty
                    } else {
                        PlaylistDetailsUiState.Success(videos)
                    }
                }
        }
    }

    fun removeVideo(videoId: String) {
        viewModelScope.launch {
            try {
                removeVideoFromPlaylistUseCase(playlist.id, videoId)
                loadPlaylistVideos()
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailsUiState.Error(e.message ?: "Erro ao remover vídeo")
            }
        }
    }
}

sealed class PlaylistDetailsUiState {
    object Loading : PlaylistDetailsUiState()
    object Empty : PlaylistDetailsUiState()
    data class Success(val videos: List<Video>) : PlaylistDetailsUiState()
    data class Error(val message: String) : PlaylistDetailsUiState()
} 