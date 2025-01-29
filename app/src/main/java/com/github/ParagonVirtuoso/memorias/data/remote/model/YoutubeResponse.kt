package com.github.ParagonVirtuoso.memorias.data.remote.model

import com.google.gson.annotations.SerializedName

data class YoutubeResponse(
    @SerializedName("items")
    val items: List<YoutubeVideo>,
    @SerializedName("nextPageToken")
    val nextPageToken: String?,
    @SerializedName("pageInfo")
    val pageInfo: PageInfo
)

data class YoutubeVideo(
    @SerializedName("id")
    val id: VideoId,
    @SerializedName("snippet")
    val snippet: VideoSnippet,
    @SerializedName("contentDetails")
    val contentDetails: ContentDetails?,
    @SerializedName("statistics")
    val statistics: Statistics?
)

data class VideoId(
    @SerializedName("videoId")
    val videoId: String
)

data class VideoSnippet(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("thumbnails")
    val thumbnails: Thumbnails,
    @SerializedName("channelTitle")
    val channelTitle: String,
    @SerializedName("publishedAt")
    val publishedAt: String
)

data class Thumbnails(
    @SerializedName("default")
    val default: Thumbnail,
    @SerializedName("medium")
    val medium: Thumbnail,
    @SerializedName("high")
    val high: Thumbnail
)

data class Thumbnail(
    @SerializedName("url")
    val url: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int
)

data class ContentDetails(
    @SerializedName("duration")
    val duration: String
)

data class Statistics(
    @SerializedName("viewCount")
    val viewCount: String
)

data class PageInfo(
    @SerializedName("totalResults")
    val totalResults: Int,
    @SerializedName("resultsPerPage")
    val resultsPerPage: Int
) 