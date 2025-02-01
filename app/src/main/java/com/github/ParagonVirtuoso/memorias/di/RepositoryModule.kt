package com.github.ParagonVirtuoso.memorias.di

import com.github.ParagonVirtuoso.memorias.data.repository.AuthRepositoryImpl
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import com.github.ParagonVirtuoso.memorias.data.repository.VideoRepositoryImpl
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
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
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository
} 