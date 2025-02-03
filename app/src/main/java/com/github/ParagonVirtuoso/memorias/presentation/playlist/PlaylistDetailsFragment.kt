package com.github.ParagonVirtuoso.memorias.presentation.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.ParagonVirtuoso.memorias.databinding.FragmentPlaylistDetailsBinding
import com.github.ParagonVirtuoso.memorias.presentation.search.VideoAdapter
import com.github.ParagonVirtuoso.memorias.util.showErrorSnackbar
import com.github.ParagonVirtuoso.memorias.util.showSuccessSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaylistDetailsFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistDetailsViewModel by viewModels()
    private val args: PlaylistDetailsFragmentArgs by navArgs()
    private val videoAdapter = VideoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeUiState()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = args.playlist.name
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.adapter = videoAdapter
        videoAdapter.onItemClick = { video ->
            val action = PlaylistDetailsFragmentDirections.actionPlaylistDetailsToVideoDetails(video)
            findNavController().navigate(action)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: PlaylistDetailsUiState) {
        binding.apply {
            progressBar.isVisible = state is PlaylistDetailsUiState.Loading
            recyclerView.isVisible = state is PlaylistDetailsUiState.Success
            emptyStateTextView.isVisible = state is PlaylistDetailsUiState.Empty

            when (state) {
                is PlaylistDetailsUiState.Success -> {
                    videoAdapter.submitList(state.videos)
                }
                is PlaylistDetailsUiState.Error -> {
                    binding.root.showErrorSnackbar(state.message)
                }
                is PlaylistDetailsUiState.Empty -> {
                    emptyStateTextView.text = "Esta playlist está vazia.\nAdicione vídeos a ela!"
                }
                is PlaylistDetailsUiState.Loading -> {
                    binding.root.showInfoSnackbar("Carregando vídeos da playlist...")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 