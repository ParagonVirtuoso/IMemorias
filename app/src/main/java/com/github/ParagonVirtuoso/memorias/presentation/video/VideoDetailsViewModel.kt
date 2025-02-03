package com.github.ParagonVirtuoso.memorias.presentation.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.FavoriteRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
import com.github.ParagonVirtuoso.memorias.domain.usecase.favorite.ToggleFavoriteUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.AddVideoToPlaylistUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.GetPlaylistsUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.CreatePlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class VideoDetailsViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addVideoToPlaylistUseCase: AddVideoToPlaylistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val playlistRepository: PlaylistRepository,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val videoRepository: VideoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val video: Video = checkNotNull(savedStateHandle["video"])

    private val _uiState = MutableStateFlow<VideoDetailsUiState>(VideoDetailsUiState.Initial)
    val uiState: StateFlow<VideoDetailsUiState> = _uiState

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            try {
                authRepository.getCurrentUser().collect { user ->
                    if (user != null) {
                        getPlaylistsUseCase(user.id)
                            .onStart { _uiState.value = VideoDetailsUiState.Loading }
                            .catch { e -> _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao carregar playlists") }
                            .collect { playlists ->
                                val playlistsWithVideoStatus = playlists.map { playlist ->
                                    PlaylistWithVideoStatus(
                                        playlist = playlist,
                                        containsVideo = playlistRepository.isVideoInPlaylist(playlist.id, video.id)
                                    )
                                }
                                _uiState.value = VideoDetailsUiState.Success(playlistsWithVideoStatus)
                            }
                    } else {
                        _uiState.value = VideoDetailsUiState.Error("Usuário não autenticado")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao carregar playlists")
            }
        }
    }

    fun addToPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                addVideoToPlaylistUseCase(playlist.id, video)
                _uiState.value = VideoDetailsUiState.VideoAddedToPlaylist
                delay(100)
                loadPlaylists()
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao adicionar vídeo à playlist")
            }
        }
    }

    fun removeFromPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.removeVideoFromPlaylist(playlist.id, video.id)
                loadPlaylists()
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao remover vídeo da playlist")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser().first()
                if (user != null) {
                    toggleFavoriteUseCase(user.id, video)
                    val isFavorite = favoriteRepository.isFavorite(user.id, video.id)
                    _uiState.value = VideoDetailsUiState.FavoriteToggled(isFavorite)
                }
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao atualizar favorito")
            }
        }
    }

    suspend fun isFavorite(): Boolean {
        val user = authRepository.getCurrentUser().first()
        return if (user != null) {
            favoriteRepository.isFavorite(user.id, video.id)
        } else {
            false
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser().first()
                if (user != null) {
                    val playlistId = createPlaylistUseCase(name, description, user.id)
                    addVideoToPlaylistUseCase(playlistId = playlistId, video = video)
                    _uiState.value = VideoDetailsUiState.VideoAddedToPlaylist
                    delay(100)
                    loadPlaylists()
                } else {
                    _uiState.value = VideoDetailsUiState.Error("Usuário não autenticado")
                }
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao criar playlist")
            }
        }
    }

    suspend fun checkInternetForPlayback(): VideoResult {
        return videoRepository.checkInternetForPlayback()
    }
}

data class PlaylistWithVideoStatus(
    val playlist: Playlist,
    val containsVideo: Boolean
)

sealed class VideoDetailsUiState {
    object Initial : VideoDetailsUiState()
    object Loading : VideoDetailsUiState()
    data class Success(val playlists: List<PlaylistWithVideoStatus>) : VideoDetailsUiState()
    object VideoAddedToPlaylist : VideoDetailsUiState()
    data class FavoriteToggled(val isFavorite: Boolean) : VideoDetailsUiState()
    data class Error(val message: String) : VideoDetailsUiState()
} 