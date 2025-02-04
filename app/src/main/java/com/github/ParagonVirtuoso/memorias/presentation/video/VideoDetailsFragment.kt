package com.github.ParagonVirtuoso.memorias.presentation.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentVideoDetailsBinding
import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.model.Video
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.presentation.memory.MemorySchedulerDialog
import com.github.ParagonVirtuoso.memorias.util.showSuccessSnackbar
import com.github.ParagonVirtuoso.memorias.util.showErrorSnackbar
import com.github.ParagonVirtuoso.memorias.util.showInfoSnackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class VideoDetailsFragment : Fragment() {

    private var _binding: FragmentVideoDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VideoDetailsViewModel by viewModels()
    private val args: VideoDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupVideoDetails()
        setupVideoPlayer()
        observeUiState()
        checkInitialFavoriteState()
        setupViews()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = args.videoTitle
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_add_to_playlist -> {
                        showPlaylistSelectionDialog()
                        true
                    }
                    R.id.action_favorite -> {
                        toggleFavorite()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupVideoDetails() {
        val videoId = args.videoId
        val videoTitle = args.videoTitle
        val videoThumbnail = args.videoThumbnail
        
        val video = Video(
            id = videoId,
            title = videoTitle,
            description = "",
            thumbnailUrl = videoThumbnail,
            channelTitle = "",
            publishedAt = ""
        )

        binding.apply {
            toolbar.title = videoTitle
            titleTextView.text = videoTitle
            descriptionTextView.text = ""
            channelTextView.text = ""
        }

        viewModel.setVideo(video)
    }

    private fun setupVideoPlayer() {
        lifecycle.addObserver(binding.youtubePlayerView)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                when (val result = viewModel.checkInternetForPlayback()) {
                    is VideoResult.Success -> {
                        initializePlayer()
                    }
                    is VideoResult.Error -> {
                        binding.root.showErrorSnackbar(result.message)
                        binding.youtubePlayerView.isVisible = false
                        binding.offlineMessage.apply {
                            isVisible = true
                            text = getString(R.string.error_no_internet_playback)
                        }
                    }
                    is VideoResult.Loading -> {
                        binding.root.showInfoSnackbar(getString(R.string.checking_connection))
                    }
                    is VideoResult.Initial -> {
                        initializePlayer()
                    }
                }
            } catch (e: Exception) {
                binding.root.showErrorSnackbar(getString(R.string.error_check_connection, e.message))
                binding.youtubePlayerView.isVisible = false
                binding.offlineMessage.apply {
                    isVisible = true
                    text = getString(R.string.error_video_playback)
                }
            }
        }
    }

    private fun initializePlayer() {
        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(args.videoId, 0f)
            }
        })
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is VideoDetailsUiState.VideoAddedToPlaylist -> {
                        showMessage(getString(R.string.video_added_to_playlist))
                    }
                    is VideoDetailsUiState.FavoriteToggled -> {
                        val menuItem = binding.toolbar.menu.findItem(R.id.action_favorite)
                        menuItem.setIcon(
                            if (state.isFavorite) R.drawable.ic_favorite
                            else R.drawable.ic_favorite_border
                        )
                        showMessage(
                            if (state.isFavorite) getString(R.string.video_added_to_favorites)
                            else getString(R.string.video_removed_from_favorites)
                        )
                    }
                    is VideoDetailsUiState.Error -> {
                        showError(state.message)
                    }
                    is VideoDetailsUiState.Loading -> {
                        // TODO: Show loading indicator
                    }
                    is VideoDetailsUiState.Success -> {
                        // TODO: Update UI with video details
                    }
                    is VideoDetailsUiState.Initial -> {
                        // TODO: Add initial state
                    }
                }
            }
        }
    }

    private fun showPlaylistSelectionDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val state = viewModel.uiState.value) {
                is VideoDetailsUiState.Success -> {
                    val playlists = state.playlists
                    if (playlists.isEmpty()) {
                        showCreatePlaylistDialog()
                        return@launch
                    }

                    val items = playlists.map { playlistStatus ->
                        if (playlistStatus.containsVideo) {
                            getString(R.string.playlist_item_with_check, playlistStatus.playlist.name)
                        } else {
                            playlistStatus.playlist.name
                        }
                    }.toMutableList()
                    items.add(getString(R.string.create_new_playlist))

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.manage_playlists))
                        .setItems(items.toTypedArray()) { _, which ->
                            if (which == items.size - 1) {
                                showCreatePlaylistDialog()
                            } else {
                                val playlistStatus = playlists[which]
                                if (playlistStatus.containsVideo) {
                                    showRemoveFromPlaylistConfirmation(playlistStatus.playlist)
                                } else {
                                    viewModel.addToPlaylist(playlistStatus.playlist)
                                }
                            }
                        }
                        .show()
                }
                is VideoDetailsUiState.Loading -> {
                    binding.root.showInfoSnackbar(getString(R.string.loading_playlist_videos))
                }
                is VideoDetailsUiState.Error -> {
                    binding.root.showErrorSnackbar(state.message)
                }
                else -> {
                    showCreatePlaylistDialog()
                }
            }
        }
    }

    private fun showRemoveFromPlaylistConfirmation(playlist: Playlist) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.remove_from_playlist))
            .setMessage(getString(R.string.remove_video_from_playlist_confirmation, playlist.name))
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                viewModel.removeFromPlaylist(playlist)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showCreatePlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.playlistNameEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.create_playlist))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.create)) { _, _ ->
                val name = nameEditText.text.toString()
                val description = descriptionEditText.text.toString()
                
                if (name.isNotBlank()) {
                    viewModel.createPlaylist(name, description)
                } else {
                    showError(getString(R.string.error_empty_playlist_name))
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun toggleFavorite() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toggleFavorite()
            delay(100)
            updateFavoriteIcon()
        }
    }

    private fun updateFavoriteIcon() {
        val menuItem = binding.toolbar.menu.findItem(R.id.action_favorite)
        viewLifecycleOwner.lifecycleScope.launch {
            val isFavorite = viewModel.isFavorite()
            menuItem.setIcon(
                if (isFavorite) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border
            )
        }
    }

    private fun showMessage(message: String) {
        binding.root.showSuccessSnackbar(message)
    }

    private fun showError(message: String) {
        binding.root.showErrorSnackbar(message)
    }

    private fun checkInitialFavoriteState() {
        viewLifecycleOwner.lifecycleScope.launch {
            updateFavoriteIcon()
        }
    }

    private fun setupViews() {
        binding.btnWatchLater.setOnClickListener {
            showMemorySchedulerDialog()
        }
    }

    private fun showMemorySchedulerDialog() {
        val videoId = args.videoId
        val videoTitle = binding.titleTextView.text.toString()
        val videoThumbnail = args.videoThumbnail

        MemorySchedulerDialog.newInstance(
            videoId = videoId,
            videoTitle = videoTitle,
            videoThumbnail = videoThumbnail,
            onScheduleCallback = { timestamp ->
                Snackbar.make(
                    binding.root,
                    getString(R.string.memory_scheduled_success, formatDateTime(timestamp)),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        ).show(childFragmentManager, MemorySchedulerDialog.TAG)
    }

    private fun formatDateTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        return dateFormat.format(Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}