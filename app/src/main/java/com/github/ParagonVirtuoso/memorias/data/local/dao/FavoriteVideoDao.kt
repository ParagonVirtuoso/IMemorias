package com.github.ParagonVirtuoso.memorias.data.local.dao

import androidx.room.*
import com.github.ParagonVirtuoso.memorias.data.local.entity.FavoriteVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteVideoDao {
    @Query("SELECT * FROM favorite_videos WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavoritesByUser(userId: String): Flow<List<FavoriteVideoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_videos WHERE videoId = :videoId AND userId = :userId)")
    suspend fun isFavorite(videoId: String, userId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favoriteVideo: FavoriteVideoEntity)

    @Query("DELETE FROM favorite_videos WHERE videoId = :videoId AND userId = :userId")
    suspend fun removeFromFavorites(videoId: String, userId: String)

    @Query("DELETE FROM favorite_videos WHERE userId = :userId")
    suspend fun clearFavorites(userId: String)
} 