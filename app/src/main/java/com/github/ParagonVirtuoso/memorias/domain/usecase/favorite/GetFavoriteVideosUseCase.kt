package com.github.ParagonVirtuoso.memorias.domain.usecase.favorite

import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteVideosUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(userId: String): Flow<List<Video>> {
        return favoriteRepository.getFavoriteVideos(userId)
    }
} 