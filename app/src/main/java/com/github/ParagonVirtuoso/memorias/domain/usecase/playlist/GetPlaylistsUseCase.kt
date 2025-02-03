package com.github.ParagonVirtuoso.memorias.domain.usecase.playlist

import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(userId: String): Flow<List<Playlist>> {
        return playlistRepository.getPlaylists(userId)
    }
} 