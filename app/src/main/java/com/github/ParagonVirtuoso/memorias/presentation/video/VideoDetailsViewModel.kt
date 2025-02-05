package com.github.ParagonVirtuoso.memorias.presentation.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.domain.model.Comment
import com.github.ParagonVirtuoso.memorias.domain.model.PlaylistWithVideoStatus
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.FavoriteRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.PlaylistRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.VideoRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.CommentRepository
import com.github.ParagonVirtuoso.memorias.domain.usecase.favorite.ToggleFavoriteUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.AddVideoToPlaylistUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.GetPlaylistsUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.playlist.CreatePlaylistUseCase
import com.github.ParagonVirtuoso.memorias.domain.usecase.video.CheckInternetConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class VideoDetailsViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addVideoToPlaylistUseCase: AddVideoToPlaylistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val playlistRepository: PlaylistRepository,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val videoRepository: VideoRepository,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val commentRepository: CommentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideoDetailsUiState>(VideoDetailsUiState.Initial)
    val uiState: StateFlow<VideoDetailsUiState> = _uiState

    private var currentVideo: Video? = null
    private var comments: List<Comment> = emptyList()
    private var nextPageToken: String? = null
    private var isLoadingMoreComments = false
    private var hasMoreComments = true
    private var isInitialLoad = true

    init {
        savedStateHandle.get<String>("videoId")?.let { videoId ->
            val videoTitle = savedStateHandle.get<String>("videoTitle") ?: ""
            val videoThumbnail = savedStateHandle.get<String>("videoThumbnail") ?: ""
            
            val video = Video(
                id = videoId,
                title = videoTitle,
                description = "",
                thumbnailUrl = videoThumbnail,
                channelTitle = "",
                publishedAt = ""
            )
            
            currentVideo = video
            loadPlaylists(video)
        }
    }

    private fun loadPlaylists(video: Video) {
        viewModelScope.launch {
            try {
                authRepository.getCurrentUser().collect { user ->
                    if (user != null) {
                        getPlaylistsUseCase(user.id)
                            .onStart { _uiState.value = VideoDetailsUiState.Loading }
                            .catch { e -> _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao carregar playlists") }
                            .collect { playlists ->
                                val playlistsWithVideoStatus = playlists.map { playlist ->
                                    PlaylistWithVideoStatus(
                                        playlist = playlist,
                                        containsVideo = playlistRepository.isVideoInPlaylist(playlist.id, video.id)
                                    )
                                }
                                _uiState.value = VideoDetailsUiState.Success(
                                    video = video,
                                    playlists = playlistsWithVideoStatus,
                                    comments = comments
                                )
                            }
                    } else {
                        _uiState.value = VideoDetailsUiState.Error("Usuário não autenticado")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao carregar playlists")
            }
        }
    }

    fun addToPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                addVideoToPlaylistUseCase(playlist.id, currentVideo!!)
                _uiState.value = VideoDetailsUiState.VideoAddedToPlaylist
                delay(100)
                loadPlaylists(currentVideo!!)
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao adicionar vídeo à playlist")
            }
        }
    }

    fun removeFromPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.removeVideoFromPlaylist(playlist.id, currentVideo!!.id)
                loadPlaylists(currentVideo!!)
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao remover vídeo da playlist")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser().first()
                if (user != null) {
                    toggleFavoriteUseCase(user.id, currentVideo!!)
                    val isFavorite = favoriteRepository.isFavorite(user.id, currentVideo!!.id)
                    _uiState.value = VideoDetailsUiState.FavoriteToggled(isFavorite)
                }
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao atualizar favorito")
            }
        }
    }

    suspend fun isFavorite(): Boolean {
        val user = authRepository.getCurrentUser().first()
        return if (user != null) {
            favoriteRepository.isFavorite(user.id, currentVideo!!.id)
        } else {
            false
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser().first()
                if (user != null) {
                    val playlistId = createPlaylistUseCase(name, description, user.id)
                    addVideoToPlaylistUseCase(playlistId = playlistId, video = currentVideo!!)
                    _uiState.value = VideoDetailsUiState.VideoAddedToPlaylist
                    delay(100)
                    loadPlaylists(currentVideo!!)
                } else {
                    _uiState.value = VideoDetailsUiState.Error("Usuário não autenticado")
                }
            } catch (e: Exception) {
                _uiState.value = VideoDetailsUiState.Error(e.message ?: "Erro ao criar playlist")
            }
        }
    }

    suspend fun checkInternetForPlayback(): VideoResult {
        return checkInternetConnectionUseCase()
    }

    fun setVideo(video: Video) {
        currentVideo = video
        loadPlaylists(video)
        loadComments()
    }

    fun isLoadingComments(): Boolean = isLoadingMoreComments

    private fun loadComments(refresh: Boolean = true) {
        if (isLoadingMoreComments) return
        if (!refresh && (nextPageToken == null || !hasMoreComments)) return

        viewModelScope.launch {
            try {
                val video = currentVideo ?: return@launch
                isLoadingMoreComments = true

                if (refresh) {
                    comments = emptyList()
                    nextPageToken = null
                    hasMoreComments = true
                    _uiState.value = VideoDetailsUiState.Loading
                }

                val commentsPage = try {
                    commentRepository.getVideoComments(
                        videoId = video.id,
                        pageToken = nextPageToken
                    )
                } catch (e: Exception) {
                    isLoadingMoreComments = false
                    if (refresh) {
                        _uiState.value = VideoDetailsUiState.Error("Erro ao carregar comentários: ${e.message}")
                    }
                    return@launch
                }

                nextPageToken = commentsPage.nextPageToken
                hasMoreComments = commentsPage.nextPageToken != null && commentsPage.comments.isNotEmpty()
                
                comments = if (refresh) {
                    commentsPage.comments
                } else {
                    comments + commentsPage.comments
                }

                when (val currentState = _uiState.value) {
                    is VideoDetailsUiState.Success -> {
                        _uiState.value = currentState.copy(
                            comments = comments,
                            playlists = currentState.playlists
                        )
                    }
                    else -> {
                        _uiState.value = VideoDetailsUiState.Success(
                            video = video,
                            playlists = emptyList(),
                            comments = comments
                        )
                    }
                }
            } catch (e: Exception) {
                if (refresh) {
                    _uiState.value = VideoDetailsUiState.Error("Erro ao carregar comentários: ${e.message}")
                }
            } finally {
                isLoadingMoreComments = false
            }
        }
    }

    fun loadMoreComments() {
        loadComments(refresh = false)
    }

    fun refreshComments() {
        loadComments(refresh = true)
    }
} 