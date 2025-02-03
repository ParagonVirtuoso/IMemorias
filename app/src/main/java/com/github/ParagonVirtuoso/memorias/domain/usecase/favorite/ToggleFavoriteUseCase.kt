package com.github.ParagonVirtuoso.memorias.domain.usecase.favorite

import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.FavoriteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(userId: String, video: Video) {
        val isFavorite = favoriteRepository.isFavorite(userId, video.id)
        if (isFavorite) {
            favoriteRepository.removeFromFavorites(userId, video.id)
        } else {
            favoriteRepository.addToFavorites(userId, video)
        }
    }
} 