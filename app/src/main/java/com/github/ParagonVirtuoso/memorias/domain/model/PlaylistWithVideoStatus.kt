package com.github.ParagonVirtuoso.memorias.domain.model

data class PlaylistWithVideoStatus(
    val playlist: Playlist,
    val containsVideo: Boolean
) 