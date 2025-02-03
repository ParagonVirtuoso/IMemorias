package com.github.ParagonVirtuoso.memorias.domain.usecase.playlist

import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import javax.inject.Inject

class UpdatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlist: Playlist) {
        playlistRepository.updatePlaylist(playlist)
    }
} 