package com.github.ParagonVirtuoso.memorias.presentation.video

import com.github.ParagonVirtuoso.memorias.domain.model.Comment
import com.github.ParagonVirtuoso.memorias.domain.model.PlaylistWithVideoStatus
import com.github.ParagonVirtuoso.memorias.domain.model.Video

sealed class VideoDetailsUiState {
    object Initial : VideoDetailsUiState()
    object Loading : VideoDetailsUiState()
    data class Success(
        val video: Video,
        val playlists: List<PlaylistWithVideoStatus>,
        val comments: List<Comment>
    ) : VideoDetailsUiState()
    data class Error(val message: String) : VideoDetailsUiState()
    object VideoAddedToPlaylist : VideoDetailsUiState()
    data class FavoriteToggled(val isFavorite: Boolean) : VideoDetailsUiState()
} 