package com.github.ParagonVirtuoso.memorias.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.CreatePlaylistUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.DeletePlaylistUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.GetPlaylistsUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.UpdatePlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val uiState: StateFlow<PlaylistUiState> = _uiState

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    getPlaylistsUseCase(user.id)
                        .onStart { _uiState.value = PlaylistUiState.Loading }
                        .catch { e -> _uiState.value = PlaylistUiState.Error(e.message ?: "Erro ao carregar playlists") }
                        .collect { playlists ->
                            _uiState.value = if (playlists.isEmpty()) {
                                PlaylistUiState.Empty
                            } else {
                                PlaylistUiState.Success(playlists)
                            }
                        }
                } else {
                    _uiState.value = PlaylistUiState.Error("Usuário não autenticado")
                }
            }
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser().first()
                if (user != null) {
                    createPlaylistUseCase(user.id, name, description)
                    _uiState.value = PlaylistUiState.PlaylistCreated
                    loadPlaylists()
                }
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Erro ao criar playlist")
            }
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                updatePlaylistUseCase(playlist)
                loadPlaylists()
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Erro ao atualizar playlist")
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                deletePlaylistUseCase(playlist.id)
                loadPlaylists()
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Erro ao excluir playlist")
            }
        }
    }
}

sealed class PlaylistUiState {
    object Loading : PlaylistUiState()
    object Empty : PlaylistUiState()
    data class Success(val playlists: List<Playlist>) : PlaylistUiState()
    data class Error(val message: String) : PlaylistUiState()
    object PlaylistCreated : PlaylistUiState()
} 