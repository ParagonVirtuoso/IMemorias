package com.github.ParagonVirtuoso.memorias.domain.usecase

import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.repository.YouTubeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchVideosUseCase @Inject constructor(
    private val youTubeRepository: YouTubeRepository
) {
    operator fun invoke(query: String): Flow<List<Video>> {
        return youTubeRepository.searchVideos(query)
    }
} 