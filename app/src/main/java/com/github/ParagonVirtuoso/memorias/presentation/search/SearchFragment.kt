package com.github.ParagonVirtuoso.memorias.presentation.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentSearchBinding
import com.github.ParagonVirtuoso.memorias.domain.model.VideoResult
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import android.widget.EditText
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private val videoAdapter = VideoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupSearchView()
        setupRecyclerView()
        observeSearchResults()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            val searchEditText = findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
            searchEditText?.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
            
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.searchVideos(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrBlank()) {
                        updateUI(VideoResult.Initial)
                    }
                    return true
                }
            })
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = videoAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        videoAdapter.onItemClick = { video ->
            findNavController().navigate(
                SearchFragmentDirections.actionSearchFragmentToVideoDetailsFragment(video)
            )
        }
    }

    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { result ->
                updateUI(result)
            }
        }
    }

    private fun updateUI(result: VideoResult) {
        when (result) {
            is VideoResult.Initial -> {
                binding.apply {
                    progressBar.isVisible = false
                    recyclerView.isVisible = false
                    emptyStateTextView.apply {
                        isVisible = true
                        text = getString(R.string.search_initial_message)
                    }
                }
            }
            is VideoResult.Loading -> {
                binding.apply {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                    emptyStateTextView.isVisible = false
                }
            }
            is VideoResult.Success -> {
                binding.apply {
                    progressBar.isVisible = false
                    emptyStateTextView.isVisible = result.videos.isEmpty()
                    if (result.videos.isEmpty()) {
                        emptyStateTextView.text = getString(R.string.no_results_found)
                    }
                    recyclerView.isVisible = result.videos.isNotEmpty()
                    videoAdapter.submitList(result.videos)
                }
            }
            is VideoResult.Error -> {
                binding.apply {
                    progressBar.isVisible = false
                    recyclerView.isVisible = false
                    emptyStateTextView.apply {
                        isVisible = true
                        text = result.message
                    }
                }
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 