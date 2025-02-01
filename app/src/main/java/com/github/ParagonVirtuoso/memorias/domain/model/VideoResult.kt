package com.github.ParagonVirtuoso.memorias.domain.model

sealed class VideoResult {
    object Initial : VideoResult()
    object Loading : VideoResult()
    data class Success(val videos: List<Video>) : VideoResult()
    data class Error(val message: String) : VideoResult()
} 