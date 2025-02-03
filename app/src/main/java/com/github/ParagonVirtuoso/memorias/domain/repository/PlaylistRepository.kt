package com.github.ParagonVirtuoso.memorias.domain.repository

import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(userId: String): Flow<List<Playlist>>
    fun getPlaylistVideos(playlistId: Long): Flow<List<Video>>
    suspend fun createPlaylist(name: String, description: String?, userId: String): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addVideoToPlaylist(playlistId: Long, video: Video)
    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: String)
    suspend fun isVideoInPlaylist(playlistId: Long, videoId: String): Boolean
} 