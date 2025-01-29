package com.github.ParagonVirtuoso.memorias.domain.model

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val publishedAt: String,
    val duration: String? = null,
    val viewCount: String? = null
)

data class VideoResponse(
    val items: List<Video>,
    val nextPageToken: String?,
    val totalResults: Int
)

sealed class VideoResult {
    data class Success(val data: VideoResponse) : VideoResult()
    data class Error(val message: String) : VideoResult()
    object Loading : VideoResult()
}

data class SearchParams(
    val query: String,
    val pageToken: String? = null,
    val maxResults: Int = 20
) 