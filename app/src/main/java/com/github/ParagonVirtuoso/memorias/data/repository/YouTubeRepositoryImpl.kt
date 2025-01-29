package com.github.ParagonVirtuoso.memorias.data.repository

import com.github.ParagonVirtuoso.memorias.BuildConfig
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.YouTubeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

interface YouTubeApi {
    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String,
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): VideoResponse
}

@Singleton
class YouTubeRepositoryImpl @Inject constructor(
    private val retrofit: Retrofit
) : YouTubeRepository {

    private val api = retrofit.create(YouTubeApi::class.java)

    override fun searchVideos(query: String): Flow<List<Video>> = flow {
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        val response = api.searchVideos("snippet", query, apiKey)
        emit(response.items.map { it.toVideo() })
    }
}

data class VideoResponse(val items: List<VideoItem>)
data class VideoItem(val id: VideoId, val snippet: Snippet) {
    fun toVideo() = Video(id.videoId, snippet.title, snippet.description)
}
data class VideoId(val videoId: String)
data class Snippet(val title: String, val description: String) 