package com.github.ParagonVirtuoso.memorias.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.data.remote.api.YoutubeApi
import com.github.ParagonVirtuoso.memorias.data.remote.model.YoutubeVideo
import com.github.ParagonVirtuoso.memorias.domain.model.SearchParams
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val youtubeApi: YoutubeApi,
    @ApplicationContext private val context: Context
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
        } catch (e: java.net.UnknownHostException) {
            emit(VideoResult.Error(context.getString(R.string.error_no_internet)))
        } catch (e: Exception) {
            emit(VideoResult.Error(e.message ?: context.getString(R.string.error_unknown)))
        }
    }

    override suspend fun getVideoDetails(videoId: String): VideoResult {
        return try {
            val response = youtubeApi.getVideoDetails(videoId = videoId)
            val video = response.items.firstOrNull()?.toDomainVideo()
            video?.let { 
                VideoResult.Success(listOf(it))
            } ?: VideoResult.Error(context.getString(R.string.error_video_not_found))
        } catch (e: java.net.UnknownHostException) {
            VideoResult.Error(context.getString(R.string.error_no_internet))
        } catch (e: Exception) {
            VideoResult.Error(e.message ?: context.getString(R.string.error_unknown))
        }
    }

    override suspend fun checkInternetForPlayback(): VideoResult {
        if (!isNetworkAvailable()) {
            return VideoResult.Error(context.getString(R.string.error_no_internet_video))
        }
        return VideoResult.Success(emptyList())
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun YoutubeVideo.toDomainVideo() = Video(
        id = id.videoId ?: "",
        title = snippet.title,
        description = snippet.description,
        thumbnailUrl = snippet.thumbnails.high.url,
        channelTitle = snippet.channelTitle,
        publishedAt = snippet.publishedAt,
        duration = contentDetails?.duration,
        viewCount = statistics?.viewCount
    )
} 