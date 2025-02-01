package com.github.ParagonVirtuoso.memorias.data.repository

import com.github.ParagonVirtuoso.memorias.data.local.cache.ThumbnailCacheManager
import com.github.ParagonVirtuoso.memorias.data.local.dao.VideoDao
import com.github.ParagonVirtuoso.memorias.data.local.entity.VideoEntity
import com.github.ParagonVirtuoso.memorias.data.remote.api.YoutubeApi
import com.github.ParagonVirtuoso.memorias.domain.model.SearchParams
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val youtubeApi: YoutubeApi,
    private val videoDao: VideoDao,
    private val thumbnailCacheManager: ThumbnailCacheManager
) : VideoRepository {

    override fun searchVideos(params: SearchParams): Flow<VideoResult> = flow {
        emit(VideoResult.Loading)
        
        try {
            // Primeiro tenta buscar do cache
            val cachedVideos = videoDao.getAllVideos()
                .map { entities -> entities.map { it.toDomainModel() } }
                .catch { emit(VideoResult.Error("Erro ao carregar cache")) }
                .collect { videos ->
                    if (videos.isNotEmpty()) {
                        emit(VideoResult.Success(videos))
                    }
                }

            // Busca da API
            val response = youtubeApi.searchVideos(
                query = params.query,
                maxResults = params.maxResults,
                pageToken = params.pageToken
            )
            val videos = response.items.map { item ->
                val video = Video(
                    id = item.id.videoId,
                    title = item.snippet.title,
                    description = item.snippet.description,
                    thumbnailUrl = item.snippet.thumbnails.high.url,
                    channelTitle = item.snippet.channelTitle,
                    publishedAt = item.snippet.publishedAt,
                    duration = item.contentDetails?.duration,
                    viewCount = item.statistics?.viewCount ?: 0L
                )

                // Cache thumbnail
                val thumbnailPath = thumbnailCacheManager.cacheThumbnail(video.id, video.thumbnailUrl)
                
                // Salva no banco local
                videoDao.insertVideo(video.toEntity(thumbnailPath))

                video
            }
            
            emit(VideoResult.Success(videos))
            
        } catch (e: Exception) {
            // Em caso de erro, tenta retornar dados do cache
            val cachedVideos = videoDao.getAllVideos().map { it.toDomainModel() }
            if (cachedVideos.isNotEmpty()) {
                emit(VideoResult.Success(cachedVideos))
            } else {
                emit(VideoResult.Error("Erro ao buscar vídeos: ${e.message}"))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getVideoDetails(videoId: String): VideoResult {
        return try {
            // Primeiro tenta buscar do cache
            videoDao.getVideoById(videoId)?.toDomainModel()?.let { video ->
                VideoResult.Success(listOf(video))
            } ?: run {
                // Se não encontrar no cache, busca da API
                val response = youtubeApi.getVideoDetails(videoId)
                response.items.firstOrNull()?.let { item ->
                    Video(
                        id = item.id,
                        title = item.snippet.title,
                        description = item.snippet.description,
                        thumbnailUrl = item.snippet.thumbnails.high.url,
                        channelTitle = item.snippet.channelTitle,
                        publishedAt = item.snippet.publishedAt,
                        duration = item.contentDetails?.duration,
                        viewCount = item.statistics.viewCount
                    ).also { video ->
                        // Cache thumbnail e salva no banco
                        val thumbnailPath = thumbnailCacheManager.cacheThumbnail(video.id, video.thumbnailUrl)
                        videoDao.insertVideo(video.toEntity(thumbnailPath))
                    }
                }?.let { video ->
                    VideoResult.Success(listOf(video))
                } ?: VideoResult.Error("Vídeo não encontrado")
            }
        } catch (e: Exception) {
            VideoResult.Error(e.message ?: "Erro desconhecido")
        }
    }

    override fun getOfflineVideos(): Flow<List<Video>> {
        return videoDao.getOfflineAvailableVideos()
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun setVideoOfflineAvailable(videoId: String, available: Boolean) {
        videoDao.updateOfflineAvailability(videoId, available)
    }

    private fun Video.toEntity(thumbnailPath: String? = null) = VideoEntity(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        channelTitle = channelTitle,
        publishedAt = publishedAt,
        viewCount = viewCount,
        lastAccessedAt = Date(),
        cachedThumbnailPath = thumbnailPath,
        isOfflineAvailable = false
    )

    private fun VideoEntity.toDomainModel() = Video(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        channelTitle = channelTitle,
        publishedAt = publishedAt,
        viewCount = viewCount
    )
} 