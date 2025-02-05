package com.github.ParagonVirtuoso.memorias.presentation.memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.FragmentMemoriesBinding
import com.github.ParagonVirtuoso.memorias.domain.model.Memory
import com.github.ParagonVirtuoso.memorias.util.showErrorSnackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MemoriesFragment : Fragment() {

    private var _binding: FragmentMemoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MemoriesViewModel by viewModels()
    private lateinit var memoriesAdapter: MemoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        viewModel.loadMemories()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        memoriesAdapter = MemoriesAdapter(
            onMemoryClick = { memory ->
                navigateToVideo(memory)
            },
            onEditClick = { memory ->
                showMemorySchedulerDialog(memory)
            },
            onDeleteClick = { memory ->
                showDeleteConfirmationDialog(memory)
            }
        )
        binding.recyclerView.adapter = memoriesAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is MemoriesUiState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.recyclerView.isVisible = false
                        binding.emptyStateTextView.isVisible = false
                    }
                    is MemoriesUiState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.recyclerView.isVisible = true
                        binding.emptyStateTextView.isVisible = false
                        memoriesAdapter.submitList(state.memories)
                    }
                    is MemoriesUiState.Empty -> {
                        binding.progressBar.isVisible = false
                        binding.recyclerView.isVisible = false
                        binding.emptyStateTextView.isVisible = true
                    }
                    is MemoriesUiState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.root.showErrorSnackbar(state.message)
                    }
                    is MemoriesUiState.Initial -> Unit
                }
            }
        }
    }

    private fun navigateToVideo(memory: Memory) {
        // TODO: Implementar navegação para o vídeo
    }

    private fun showMemorySchedulerDialog(memory: Memory) {
        MemorySchedulerDialog.newInstance(
            videoId = memory.videoId,
            videoTitle = memory.videoTitle,
            videoThumbnail = memory.videoThumbnail,
            onScheduleCallback = { timestamp ->
                val updatedMemory = memory.copy(notificationTime = Date(timestamp))
                viewModel.updateMemory(updatedMemory)
                showSuccessMessage(timestamp)
            }
        ).show(childFragmentManager, MemorySchedulerDialog.TAG)
    }

    private fun showSuccessMessage(timestamp: Long) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        val formattedDate = dateFormat.format(Date(timestamp))
        Snackbar.make(
            binding.root,
            getString(R.string.memory_scheduled_success, formattedDate),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun showDeleteConfirmationDialog(memory: Memory) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_memory)
            .setMessage(R.string.delete_memory_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteMemory(memory)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 