package com.github.ParagonVirtuoso.memorias.domain.usecase.video

import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
import javax.inject.Inject

class CheckInternetConnectionUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(): VideoResult {
        return videoRepository.checkInternetForPlayback()
    }
} 