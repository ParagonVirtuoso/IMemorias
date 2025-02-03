package com.github.ParagonVirtuoso.memorias.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Long,
    val name: String,
    val description: String?,
    val userId: String,
    val createdAt: Long
) : Parcelable 