package com.github.ParagonVirtuoso.memorias.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val publishedAt: String,
    val duration: String? = null,
    val viewCount: String? = null
) : Parcelable

data class VideoResponse(
    val videos: List<Video>,
    val nextPageToken: String?,
    val totalResults: Int
)

data class SearchParams(
    val query: String,
    val maxResults: Int = 20,
    val pageToken: String? = null
) 