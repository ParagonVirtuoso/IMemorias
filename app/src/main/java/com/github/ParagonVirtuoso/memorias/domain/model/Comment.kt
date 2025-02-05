package com.github.ParagonVirtuoso.memorias.domain.model

data class Comment(
    val id: String,
    val authorName: String,
    val text: String,
    val likeCount: Long,
    val publishedAt: String
) 