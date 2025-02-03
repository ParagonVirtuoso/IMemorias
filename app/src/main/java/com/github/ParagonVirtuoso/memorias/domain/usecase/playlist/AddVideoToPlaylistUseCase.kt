package com.github.ParagonVirtuoso.memorias.domain.usecase.playlist

import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddVideoToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, video: Video) {
        playlistRepository.addVideoToPlaylist(playlistId, video)
    }
} 