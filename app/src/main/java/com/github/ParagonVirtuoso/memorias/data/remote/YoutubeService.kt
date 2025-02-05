package com.github.ParagonVirtuoso.memorias.data.remote

import com.github.ParagonVirtuoso.memorias.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface YoutubeService {
    @GET("commentThreads")
    suspend fun getVideoComments(
        @Query("part") part: String = "snippet",
        @Query("videoId") videoId: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String = BuildConfig.YOUTUBE_API_KEY
    ): CommentThreadListResponse
}

data class CommentThreadListResponse(
    val items: List<CommentThread>,
    val nextPageToken: String?,
    val pageInfo: PageInfo
)

data class PageInfo(
    val totalResults: Int,
    val resultsPerPage: Int
)

data class CommentThread(
    val id: String,
    val snippet: CommentSnippet
)

data class CommentSnippet(
    val topLevelComment: TopLevelComment
)

data class TopLevelComment(
    val id: String,
    val snippet: CommentDetailSnippet
)

data class CommentDetailSnippet(
    val authorDisplayName: String,
    val textDisplay: String,
    val likeCount: Long,
    val publishedAt: String
) 