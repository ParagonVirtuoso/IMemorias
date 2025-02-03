package com.github.ParagonVirtuoso.memorias.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import com.github.ParagonVirtuoso.memorias.domain.usecase.favorite.GetFavoriteVideosUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.favorite.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getFavoriteVideosUseCase: GetFavoriteVideosUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoriteUiState>(FavoriteUiState.Loading)
    val uiState: StateFlow<FavoriteUiState> = _uiState

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    getFavoriteVideosUseCase(user.id)
                        .onStart { _uiState.value = FavoriteUiState.Loading }
                        .catch { e -> _uiState.value = FavoriteUiState.Error(e.message ?: "Erro ao carregar favoritos") }
                        .collect { videos ->
                            _uiState.value = if (videos.isEmpty()) {
                                FavoriteUiState.Empty
                            } else {
                                FavoriteUiState.Success(videos)
                            }
                        }
                } else {
                    _uiState.value = FavoriteUiState.Error("Usuário não autenticado")
                }
            }
        }
    }

    fun toggleFavorite(video: Video) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser().first()
                if (user != null) {
                    toggleFavoriteUseCase(user.id, video)
                    loadFavorites()
                }
            } catch (e: Exception) {
                _uiState.value = FavoriteUiState.Error(e.message ?: "Erro ao atualizar favorito")
            }
        }
    }
}

sealed class FavoriteUiState {
    object Loading : FavoriteUiState()
    object Empty : FavoriteUiState()
    data class Success(val videos: List<Video>) : FavoriteUiState()
    data class Error(val message: String) : FavoriteUiState()
} 