package com.github.ParagonVirtuoso.memorias.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.ParagonVirtuoso.memorias.data.local.dao.FavoriteVideoDao
import com.github.ParagonVirtuoso.memorias.data.local.dao.PlaylistDao
import com.github.ParagonVirtuoso.memorias.data.local.dao.PlaylistVideoDao
import com.github.ParagonVirtuoso.memorias.data.local.entity.FavoriteVideoEntity
import com.github.ParagonVirtuoso.memorias.data.local.entity.PlaylistEntity
import com.github.ParagonVirtuoso.memorias.data.local.entity.PlaylistVideoEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistVideoEntity::class,
        FavoriteVideoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class IMemoriasDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistVideoDao(): PlaylistVideoDao
    abstract fun favoriteVideoDao(): FavoriteVideoDao
} 