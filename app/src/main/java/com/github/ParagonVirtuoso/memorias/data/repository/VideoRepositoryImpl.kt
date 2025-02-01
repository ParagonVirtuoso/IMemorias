package com.github.ParagonVirtuoso.memorias.data.repository

import com.github.ParagonVirtuoso.memorias.data.remote.api.YoutubeApi
import com.github.ParagonVirtuoso.memorias.data.remote.model.YoutubeVideo
import com.github.ParagonVirtuoso.memorias.domain.model.SearchParams
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val youtubeApi: YoutubeApi
) : VideoRepository {

    override fun searchVideos(params: SearchParams): Flow<VideoResult> = flow {
        emit(VideoResult.Loading)
        try {
            val response = youtubeApi.searchVideos(
                query = params.query,
                maxResults = params.maxResults,
                pageToken = params.pageToken
            )
            
            val videos = response.items.map { it.toDomainVideo() }
            emit(VideoResult.Success(videos))
        } catch (e: Exception) {
            emit(VideoResult.Error(e.message ?: "Erro desconhecido"))
        }
    }

    override suspend fun getVideoDetails(videoId: String): VideoResult {
        return try {
            val response = youtubeApi.getVideoDetails(videoId = videoId)
            val video = response.items.firstOrNull()?.toDomainVideo()
            video?.let { 
                VideoResult.Success(listOf(it))
            } ?: VideoResult.Error("Vídeo não encontrado")
        } catch (e: Exception) {
            VideoResult.Error(e.message ?: "Erro desconhecido")
        }
    }

    private fun YoutubeVideo.toDomainVideo() = Video(
        id = id.videoId,
        title = snippet.title,
        description = snippet.description,
        thumbnailUrl = snippet.thumbnails.high.url,
        channelTitle = snippet.channelTitle,
        publishedAt = snippet.publishedAt,
        duration = contentDetails?.duration,
        viewCount = statistics?.viewCount
    )
} 