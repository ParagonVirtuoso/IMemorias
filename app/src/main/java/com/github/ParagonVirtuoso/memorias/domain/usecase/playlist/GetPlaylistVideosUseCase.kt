package com.github.ParagonVirtuoso.memorias.domain.usecase.playlist

import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistVideosUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long): Flow<List<Video>> {
        return playlistRepository.getPlaylistVideos(playlistId)
    }
} 