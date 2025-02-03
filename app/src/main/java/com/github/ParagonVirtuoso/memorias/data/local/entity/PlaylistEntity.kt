package com.github.ParagonVirtuoso.memorias.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
) 