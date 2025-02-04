package com.github.ParagonVirtuoso.memorias.presentation.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.ParagonVirtuoso.memorias.databinding.FragmentFavoritesBinding
import com.github.ParagonVirtuoso.memorias.presentation.search.VideoAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteViewModel by viewModels()
    private val videoAdapter = VideoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeUiState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.adapter = videoAdapter
        videoAdapter.onItemClick = { video ->
            val action = FavoriteFragmentDirections.actionFavoriteToVideoDetails(
                videoId = video.id,
                videoTitle = video.title,
                videoThumbnail = video.thumbnailUrl
            )
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

    private fun updateUI(state: FavoriteUiState) {
        binding.apply {
            progressBar.isVisible = state is FavoriteUiState.Loading
            recyclerView.isVisible = state is FavoriteUiState.Success
            emptyStateTextView.isVisible = state is FavoriteUiState.Empty

            when (state) {
                is FavoriteUiState.Success -> videoAdapter.submitList(state.videos)
                is FavoriteUiState.Error -> showError(state.message)
                else -> Unit
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 