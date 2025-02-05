package com.github.ParagonVirtuoso.memorias.di

import com.github.ParagonVirtuoso.memorias.data.repository.AuthRepositoryImpl
import com.github.ParagonVirtuoso.memorias.data.repository.FavoriteRepositoryImpl
import com.github.ParagonVirtuoso.memorias.data.repository.PlaylistRepositoryImpl
import com.github.ParagonVirtuoso.memorias.data.repository.VideoRepositoryImpl
import com.github.ParagonVirtuoso.memorias.data.repository.CommentRepositoryImpl
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.FavoriteRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.CommentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        videoRepositoryImpl: VideoRepositoryImpl
    ): VideoRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository
} 