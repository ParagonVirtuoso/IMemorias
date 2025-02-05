package com.github.ParagonVirtuoso.memorias.presentation.memory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.ParagonVirtuoso.memorias.domain.repository.MemoryRepository
import com.github.ParagonVirtuoso.memorias.worker.MemoryNotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MemorySchedulerViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemorySchedulerState>(MemorySchedulerState.Initial)
    val uiState: StateFlow<MemorySchedulerState> = _uiState

    fun scheduleMemory(
        videoId: String,
        videoTitle: String,
        videoThumbnail: String,
        notificationTime: Date
    ) {
        viewModelScope.launch {
            try {
                val delay = notificationTime.time - System.currentTimeMillis()
                
                if (delay <= 0) {
                    _uiState.value = MemorySchedulerState.Error("A data selecionada deve ser no futuro")
                    return@launch
                }

                val result = memoryRepository.createMemory(
                    videoId = videoId,
                    videoTitle = videoTitle,
                    videoThumbnail = videoThumbnail,
                    notificationTime = notificationTime
                )

                result.fold(
                    onSuccess = { memory ->
                        _uiState.value = MemorySchedulerState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = MemorySchedulerState.Error("Erro ao salvar memória: ${exception.message}")
                    }
                )

            } catch (e: Exception) {
                _uiState.value = MemorySchedulerState.Error("Erro ao agendar memória: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = MemorySchedulerState.Initial
    }
} 