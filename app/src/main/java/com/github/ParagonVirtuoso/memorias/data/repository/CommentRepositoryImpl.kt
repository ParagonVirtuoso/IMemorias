package com.github.ParagonVirtuoso.memorias.data.repository

import com.github.ParagonVirtuoso.memorias.data.remote.YoutubeService
import com.github.ParagonVirtuoso.memorias.domain.model.Comment
import com.github.ParagonVirtuoso.memorias.domain.repository.CommentRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.CommentsPage
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val youtubeService: YoutubeService
) : CommentRepository {

    override suspend fun getVideoComments(videoId: String, pageToken: String?): CommentsPage {
        return try {
            val response = youtubeService.getVideoComments(
                videoId = videoId,
                pageToken = pageToken
            )
            
            CommentsPage(
                comments = response.items.map { commentThread ->
                    val snippet = commentThread.snippet.topLevelComment.snippet
                    Comment(
                        id = commentThread.id,
                        authorName = snippet.authorDisplayName,
                        text = snippet.textDisplay,
                        likeCount = snippet.likeCount,
                        publishedAt = snippet.publishedAt
                    )
                },
                nextPageToken = response.nextPageToken,
                totalResults = response.pageInfo.totalResults
            )
        } catch (e: Exception) {
            throw Exception("Erro ao carregar comentários: ${e.message}")
        }
    }
}