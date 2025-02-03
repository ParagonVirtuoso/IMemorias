package com.github.ParagonVirtuoso.memorias.domain.usecase.playlist

import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import javax.inject.Inject

class RemoveVideoFromPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, videoId: String) {
        playlistRepository.removeVideoFromPlaylist(playlistId, videoId)
    }
} 