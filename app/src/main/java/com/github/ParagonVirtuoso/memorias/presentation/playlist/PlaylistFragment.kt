package com.github.ParagonVirtuoso.memorias.presentation.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentPlaylistBinding
import com.github.ParagonVirtuoso.memorias.domain.model.Playlist
import com.github.ParagonVirtuoso.memorias.util.showErrorSnackbar
import com.github.ParagonVirtuoso.memorias.util.showSuccessSnackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels()
    private val playlistAdapter = PlaylistAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeUiState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.adapter = playlistAdapter
        playlistAdapter.onItemClick = { playlist ->
            navigateToPlaylistDetails(playlist)
        }
        playlistAdapter.onMenuClick = { playlist ->
            showPlaylistOptionsDialog(playlist)
        }
    }

    private fun setupClickListeners() {
        binding.addPlaylistFab.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: PlaylistUiState) {
        binding.apply {
            progressBar.isVisible = state is PlaylistUiState.Loading
            recyclerView.isVisible = state is PlaylistUiState.Success
            emptyStateTextView.isVisible = state is PlaylistUiState.Empty

            when (state) {
                is PlaylistUiState.Success -> playlistAdapter.submitList(state.playlists)
                is PlaylistUiState.Error -> binding.root.showErrorSnackbar(state.message)
                is PlaylistUiState.PlaylistCreated -> binding.root.showSuccessSnackbar(getString(R.string.playlist_created))
                else -> Unit
            }
        }
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

    private fun showPlaylistOptionsDialog(playlist: Playlist) {
        val options = arrayOf(getString(R.string.edit), getString(R.string.delete))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.playlist_options))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditPlaylistDialog(playlist)
                    1 -> showDeletePlaylistConfirmation(playlist)
                }
            }
            .show()
    }

    private fun showEditPlaylistDialog(playlist: Playlist) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)

        val nameEditText = dialogView.findViewById<TextInputEditText>(R.id.playlistNameEditText)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.descriptionEditText)

        nameEditText.setText(playlist.name)
        descriptionEditText.setText(playlist.description)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.edit_playlist))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = nameEditText.text?.toString()?.trim()
                val description = descriptionEditText.text?.toString()?.trim()

                if (!name.isNullOrEmpty()) {
                    viewModel.updatePlaylist(playlist.copy(
                        name = name,
                        description = description.orEmpty()
                    ))
                } else {
                    showError(getString(R.string.error_empty_playlist_name))
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDeletePlaylistConfirmation(playlist: Playlist) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_playlist))
            .setMessage(getString(R.string.delete_playlist_confirmation, playlist.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deletePlaylist(playlist)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun navigateToPlaylistDetails(playlist: Playlist) {
        try {
            val action = PlaylistFragmentDirections.actionPlaylistToPlaylistDetails(playlist)
            findNavController().navigate(action)
        } catch (e: Exception) {
            binding.root.showErrorSnackbar(getString(R.string.error_open_playlist, e.message))
        }
    }

    private fun showError(message: String) {
        binding.root.showErrorSnackbar(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 