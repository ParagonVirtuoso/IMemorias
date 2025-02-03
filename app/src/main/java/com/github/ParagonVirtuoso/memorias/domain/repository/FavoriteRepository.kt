package com.github.ParagonVirtuoso.memorias.domain.repository

import com.github.ParagonVirtuoso.memorias.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavoriteVideos(userId: String): Flow<List<Video>>
    suspend fun addToFavorites(userId: String, video: Video)
    suspend fun removeFromFavorites(userId: String, videoId: String)
    suspend fun isFavorite(userId: String, videoId: String): Boolean
    suspend fun clearFavorites(userId: String)
} 