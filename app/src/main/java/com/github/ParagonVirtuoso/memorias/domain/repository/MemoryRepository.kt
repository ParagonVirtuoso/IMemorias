package com.github.ParagonVirtuoso.memorias.domain.repository

import com.github.ParagonVirtuoso.memorias.domain.model.Memory
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface MemoryRepository {
    suspend fun createMemory(
        videoId: String,
        videoTitle: String,
        videoThumbnail: String,
        notificationTime: Date
    ): Result<Memory>

    suspend fun getMemories(): Result<List<Memory>>
    
    suspend fun deleteMemory(memoryId: String): Result<Unit>
    
    suspend fun updateMemory(memory: Memory): Result<Memory>

    fun getMemories(userId: String): Flow<List<Memory>>

    suspend fun markAsNotified(memoryId: String): Result<Unit>

    suspend fun scheduleMemoryNotification(memory: Memory): Result<Unit>
} 