package com.github.ParagonVirtuoso.memorias.domain.repository

import com.github.ParagonVirtuoso.memorias.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface YouTubeRepository {
    fun searchVideos(query: String): Flow<List<Video>>
} 