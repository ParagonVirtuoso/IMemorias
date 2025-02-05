package com.github.ParagonVirtuoso.memorias.presentation.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.model.Memory
import com.github.ParagonVirtuoso.memorias.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoriesViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemoriesUiState>(MemoriesUiState.Initial)
    val uiState: StateFlow<MemoriesUiState> = _uiState

    init {
        loadMemories()
    }

    fun loadMemories() {
        viewModelScope.launch {
            _uiState.value = MemoriesUiState.Loading

            memoryRepository.getMemories()
                .onSuccess { memories ->
                    _uiState.value = if (memories.isEmpty()) {
                        MemoriesUiState.Empty
                    } else {
                        MemoriesUiState.Success(memories)
                    }
                }
                .onFailure { exception ->
                    _uiState.value = MemoriesUiState.Error(exception.message ?: "Erro ao carregar memórias")
                }
        }
    }

    fun deleteMemory(memory: Memory) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(memory.id)
                .onSuccess {
                    loadMemories()
                }
                .onFailure { exception ->
                    _uiState.value = MemoriesUiState.Error(exception.message ?: "Erro ao excluir memória")
                }
        }
    }

    fun updateMemory(memory: Memory) {
        viewModelScope.launch {
            memoryRepository.updateMemory(memory)
                .onSuccess {
                    loadMemories()
                }
                .onFailure { exception ->
                    _uiState.value = MemoriesUiState.Error(exception.message ?: "Erro ao atualizar memória")
                }
        }
    }
}

sealed class MemoriesUiState {
    object Initial : MemoriesUiState()
    object Loading : MemoriesUiState()
    object Empty : MemoriesUiState()
    data class Success(val memories: List<Memory>) : MemoriesUiState()
    data class Error(val message: String) : MemoriesUiState()
} 