package com.github.ParagonVirtuoso.memorias.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

fun formatLikeCount(count: Long): String {
    return when {
        count < 1000 -> count.toString()
        count < 1_000_000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%.1fM", count / 1_000_000.0)
    }
}

fun formatTimeAgo(publishedAt: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    
    try {
        val publishedDate = dateFormat.parse(publishedAt) ?: return "agora"
        val now = Date()
        val diffInMillis = now.time - publishedDate.time
        val diffInMinutes = diffInMillis / (60 * 1000)
        val diffInHours = diffInMinutes / 60
        val diffInDays = diffInHours / 24
        val diffInMonths = diffInDays / 30
        val diffInYears = diffInDays / 365

        return when {
            diffInMinutes < 1 -> "agora"
            diffInMinutes < 60 -> "há ${diffInMinutes} minutos"
            diffInHours < 24 -> "há ${diffInHours} horas"
            diffInDays < 30 -> "há ${diffInDays} dias"
            diffInMonths < 12 -> "há ${diffInMonths} meses"
            else -> "há ${diffInYears} anos"
        }
    } catch (e: Exception) {
        return "data desconhecida"
    }
} 