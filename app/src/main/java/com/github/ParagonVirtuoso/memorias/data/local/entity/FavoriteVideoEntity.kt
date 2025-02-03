package com.github.ParagonVirtuoso.memorias.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_videos")
data class FavoriteVideoEntity(
    @PrimaryKey
    val videoId: String,
    val userId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val publishedAt: String,
    val duration: String?,
    val viewCount: String?,
    val addedAt: Long = System.currentTimeMillis()
) 