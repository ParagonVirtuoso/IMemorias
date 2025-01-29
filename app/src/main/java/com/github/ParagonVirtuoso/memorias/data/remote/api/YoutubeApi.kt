package com.github.ParagonVirtuoso.memorias.data.remote.api

import com.github.ParagonVirtuoso.memorias.data.remote.model.YoutubeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApi {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int,
        @Query("pageToken") pageToken: String? = null
    ): YoutubeResponse

    @GET("youtube/v3/videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails,statistics",
        @Query("id") videoId: String
    ): YoutubeResponse

    companion object {
        const val BASE_URL = "https://www.googleapis.com/"
    }
} 