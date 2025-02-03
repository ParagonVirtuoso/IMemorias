package com.github.ParagonVirtuoso.memorias.data.repository

import com.github.ParagonVirtuoso.memorias.data.local.dao.FavoriteVideoDao
import com.github.ParagonVirtuoso.memorias.data.local.entity.FavoriteVideoEntity
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteVideoDao: FavoriteVideoDao
) : FavoriteRepository {

    override fun getFavoriteVideos(userId: String): Flow<List<Video>> {
        return favoriteVideoDao.getFavoritesByUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addToFavorites(userId: String, video: Video) {
        val favoriteVideo = FavoriteVideoEntity(
            videoId = video.id,
            userId = userId,
            title = video.title,
            description = video.description,
            thumbnailUrl = video.thumbnailUrl,
            channelTitle = video.channelTitle,
            publishedAt = video.publishedAt,
            duration = video.duration,
            viewCount = video.viewCount
        )
        favoriteVideoDao.addToFavorites(favoriteVideo)
    }

    override suspend fun removeFromFavorites(userId: String, videoId: String) {
        favoriteVideoDao.removeFromFavorites(videoId, userId)
    }

    override suspend fun isFavorite(userId: String, videoId: String): Boolean {
        return favoriteVideoDao.isFavorite(videoId, userId)
    }

    override suspend fun clearFavorites(userId: String) {
        favoriteVideoDao.clearFavorites(userId)
    }

    private fun FavoriteVideoEntity.toDomainModel() = Video(
        id = videoId,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        channelTitle = channelTitle,
        publishedAt = publishedAt,
        duration = duration,
        viewCount = viewCount
    )
} 