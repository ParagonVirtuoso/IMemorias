package com.github.ParagonVirtuoso.memorias.domain.repository

import com.github.ParagonVirtuoso.memorias.domain.model.Comment

data class CommentsPage(
    val comments: List<Comment>,
    val nextPageToken: String?,
    val totalResults: Int
)

interface CommentRepository {
    suspend fun getVideoComments(videoId: String, pageToken: String? = null): CommentsPage
} 