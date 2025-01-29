package com.github.ParagonVirtuoso.memorias.domain.repository

import com.github.ParagonVirtuoso.memorias.domain.model.SearchParams
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun searchVideos(params: SearchParams): Flow<VideoResult>
    suspend fun getVideoDetails(videoId: String): VideoResult
} 