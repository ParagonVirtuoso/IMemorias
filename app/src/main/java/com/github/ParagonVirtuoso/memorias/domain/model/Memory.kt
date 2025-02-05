package com.github.ParagonVirtuoso.memorias.domain.model

import java.util.Date

data class Memory(
    val id: String = "",
    val userId: String = "",
    val videoId: String = "",
    val videoTitle: String = "",
    val videoThumbnail: String = "",
    val notificationTime: Date = Date(),
    val createdAt: Date = Date(),
    val notified: Boolean = false
) 