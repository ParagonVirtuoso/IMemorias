package com.github.ParagonVirtuoso.memorias.di

import android.content.Context
import androidx.room.Room
import com.github.ParagonVirtuoso.memorias.data.local.IMemoriasDatabase
import com.github.ParagonVirtuoso.memorias.data.local.dao.FavoriteVideoDao
import com.github.ParagonVirtuoso.memorias.data.local.dao.PlaylistDao
import com.github.ParagonVirtuoso.memorias.data.local.dao.PlaylistVideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): IMemoriasDatabase {
        return Room.databaseBuilder(
            context,
            IMemoriasDatabase::class.java,
            "imemorias.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun providePlaylistDao(database: IMemoriasDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun providePlaylistVideoDao(database: IMemoriasDatabase): PlaylistVideoDao {
        return database.playlistVideoDao()
    }

    @Provides
    fun provideFavoriteVideoDao(database: IMemoriasDatabase): FavoriteVideoDao {
        return database.favoriteVideoDao()
    }
} 