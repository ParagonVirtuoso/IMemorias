package com.github.ParagonVirtuoso.memorias.data.local.dao

import androidx.room.*
import com.github.ParagonVirtuoso.memorias.data.local.entity.PlaylistVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistVideoDao {
    @Query("SELECT * FROM playlist_videos WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun getVideosByPlaylist(playlistId: Long): Flow<List<PlaylistVideoEntity>>

    @Query("SELECT COUNT(*) FROM playlist_videos WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun isVideoInPlaylist(playlistId: Long, videoId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVideoToPlaylist(playlistVideo: PlaylistVideoEntity)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: String)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
} 