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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentVideoDetailsBinding
import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.github.ParagonVirtuoso.memorias.util.showSuccessSnackbar
import com.github.ParagonVirtuoso.memorias.util.showErrorSnackbar
import com.github.ParagonVirtuoso.memorias.util.showInfoSnackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = args.video.title
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
        val video = args.video
        binding.apply {
            toolbar.title = video.title
            titleTextView.text = video.title
            descriptionTextView.text = video.description
            channelTextView.text = video.channelTitle
        }
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
                            text = "Não é possível reproduzir vídeos sem conexão com a internet."
                        }
                    }
                    is VideoResult.Loading -> {
                        binding.root.showInfoSnackbar("Verificando conexão...")
                    }
                    is VideoResult.Initial -> {
                        initializePlayer()
                    }
                }
            } catch (e: Exception) {
                binding.root.showErrorSnackbar("Erro ao verificar conexão. ${e.message}")
                binding.youtubePlayerView.isVisible = false
                binding.offlineMessage.apply {
                    isVisible = true
                    text = "Ocorreu um erro ao tentar reproduzir o vídeo."
                }
            }
        }
    }

    private fun initializePlayer() {
        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(args.video.id, 0f)
            }
        })
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is VideoDetailsUiState.VideoAddedToPlaylist -> {
                        showMessage("Vídeo adicionado à playlist com sucesso!")
                    }
                    is VideoDetailsUiState.FavoriteToggled -> {
                        val menuItem = binding.toolbar.menu.findItem(R.id.action_favorite)
                        menuItem.setIcon(
                            if (state.isFavorite) R.drawable.ic_favorite
                            else R.drawable.ic_favorite_border
                        )
                        showMessage(
                            if (state.isFavorite) "Vídeo adicionado aos favoritos"
                            else "Vídeo removido dos favoritos"
                        )
                    }
                    is VideoDetailsUiState.Error -> {
                        showError(state.message)
                    }
                    is VideoDetailsUiState.Loading -> {
                        // Poderia mostrar um loading se necessário
                    }
                    is VideoDetailsUiState.Success -> {
                        // Estado atualizado com sucesso, não precisa fazer nada aqui
                    }
                    is VideoDetailsUiState.Initial -> {
                        // Estado inicial, não precisa fazer nada aqui
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
                            "${playlistStatus.playlist.name} ✓"
                        } else {
                            playlistStatus.playlist.name
                        }
                    }.toMutableList()
                    items.add("+ Criar nova playlist")

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Gerenciar playlists")
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
                    binding.root.showInfoSnackbar("Carregando playlists...")
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
            .setTitle("Remover da playlist")
            .setMessage("Deseja remover este vídeo da playlist '${playlist.name}'?")
            .setPositiveButton("Remover") { _, _ ->
                viewModel.removeFromPlaylist(playlist)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showCreatePlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.playlistNameEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Criar nova playlist")
            .setView(dialogView)
            .setPositiveButton("Criar") { _, _ ->
                val name = nameEditText.text.toString()
                val description = descriptionEditText.text.toString()
                
                if (name.isNotBlank()) {
                    viewModel.createPlaylist(name, description)
                } else {
                    showError("O nome da playlist não pode estar vazio")
                }
            }
            .setNegativeButton("Cancelar", null)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 