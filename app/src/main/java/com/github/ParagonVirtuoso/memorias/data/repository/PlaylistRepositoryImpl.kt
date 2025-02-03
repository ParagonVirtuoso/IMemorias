package com.github.ParagonVirtuoso.memorias.data.repository

import com.github.ParagonVirtuoso.memorias.data.local.dao.PlaylistDao
import com.github.ParagonVirtuoso.memorias.data.local.dao.PlaylistVideoDao
import com.github.ParagonVirtuoso.memorias.data.local.entity.PlaylistEntity
import com.github.ParagonVirtuoso.memorias.data.local.entity.PlaylistVideoEntity
import com.github.ParagonVirtuoso.memorias.data.remote.api.YoutubeApi
import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import android.util.Log

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistVideoDao: PlaylistVideoDao,
    private val youtubeApi: YoutubeApi
) : PlaylistRepository {

    override fun getPlaylists(userId: String): Flow<List<Playlist>> {
        return playlistDao.getPlaylistsByUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    override fun getPlaylistVideos(playlistId: Long): Flow<List<Video>> {
        return playlistVideoDao.getVideosByPlaylist(playlistId)
            .onEach { entities ->
                entities.filter { it.thumbnailUrl.isEmpty() }.forEach { playlistVideo ->
                    try {
                        val response = youtubeApi.getVideoDetails(playlistVideo.videoId)
                        response.items.firstOrNull()?.let { youtubeVideo ->
                            val updatedVideo = PlaylistVideoEntity(
                                playlistId = playlistVideo.playlistId,
                                videoId = playlistVideo.videoId,
                                title = youtubeVideo.snippet.title,
                                description = youtubeVideo.snippet.description,
                                thumbnailUrl = youtubeVideo.snippet.thumbnails.high.url,
                                channelTitle = youtubeVideo.snippet.channelTitle,
                                publishedAt = youtubeVideo.snippet.publishedAt,
                                duration = youtubeVideo.contentDetails?.duration,
                                viewCount = youtubeVideo.statistics?.viewCount,
                                addedAt = playlistVideo.addedAt
                            )
                            playlistVideoDao.addVideoToPlaylist(updatedVideo)
                        }
                    } catch (e: Exception) {
                        Log.e("PlaylistRepositoryImpl", "Error updating video with ID '${playlistVideo.videoId}': ${e.localizedMessage}", e)
                    }
                }
            }
            .map { entities ->
                entities.map { playlistVideo ->
                    Video(
                        id = playlistVideo.videoId,
                        title = playlistVideo.title,
                        description = playlistVideo.description,
                        thumbnailUrl = playlistVideo.thumbnailUrl,
                        channelTitle = playlistVideo.channelTitle,
                        publishedAt = playlistVideo.publishedAt,
                        duration = playlistVideo.duration,
                        viewCount = playlistVideo.viewCount
                    )
                }
            }
    }

    override suspend fun createPlaylist(name: String, description: String?, userId: String): Long {
        val playlist = PlaylistEntity(
            name = name,
            description = description,
            userId = userId
        )
        return playlistDao.insertPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val playlist = playlistDao.getPlaylistById(playlistId)
        playlist?.let {
            playlistDao.deletePlaylist(it)
        }
    }

    override suspend fun addVideoToPlaylist(playlistId: Long, video: Video) {
        try {
            val response = youtubeApi.getVideoDetails(video.id)
            response.items.firstOrNull()?.let { youtubeVideo ->
                val playlistVideo = PlaylistVideoEntity(
                    playlistId = playlistId,
                    videoId = video.id,
                    title = youtubeVideo.snippet.title,
                    description = youtubeVideo.snippet.description,
                    thumbnailUrl = youtubeVideo.snippet.thumbnails.high.url,
                    channelTitle = youtubeVideo.snippet.channelTitle,
                    publishedAt = youtubeVideo.snippet.publishedAt,
                    duration = youtubeVideo.contentDetails?.duration,
                    viewCount = youtubeVideo.statistics?.viewCount
                )
                playlistVideoDao.addVideoToPlaylist(playlistVideo)
            }
        } catch (e: Exception) {
            val playlistVideo = PlaylistVideoEntity(
                playlistId = playlistId,
                videoId = video.id,
                title = video.title,
                description = video.description,
                thumbnailUrl = video.thumbnailUrl,
                channelTitle = video.channelTitle,
                publishedAt = video.publishedAt,
                duration = video.duration,
                viewCount = video.viewCount
            )
            playlistVideoDao.addVideoToPlaylist(playlistVideo)
        }
    }

    override suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: String) {
        playlistVideoDao.removeVideoFromPlaylist(playlistId, videoId)
    }

    override suspend fun isVideoInPlaylist(playlistId: Long, videoId: String): Boolean {
        return playlistVideoDao.isVideoInPlaylist(playlistId, videoId) > 0
    }

    private fun PlaylistEntity.toDomainModel() = Playlist(
        id = id,
        name = name,
        description = description,
        userId = userId,
        createdAt = createdAt
    )

    private fun Playlist.toEntity() = PlaylistEntity(
        id = id,
        name = name,
        description = description,
        userId = userId,
        createdAt = createdAt
    )
} 