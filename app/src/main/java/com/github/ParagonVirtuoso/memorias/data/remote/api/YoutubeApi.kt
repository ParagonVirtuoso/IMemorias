package com.github.ParagonVirtuoso.memorias.data.remote.api

import com.github.ParagonVirtuoso.memorias.BuildConfig
import com.github.ParagonVirtuoso.memorias.data.remote.model.YoutubeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApi {
    @GET("search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String = BuildConfig.YOUTUBE_API_KEY
    ): YoutubeResponse

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("id") videoId: String,
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("key") apiKey: String = BuildConfig.YOUTUBE_API_KEY
    ): YoutubeResponse

    companion object {
        const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    }
} 