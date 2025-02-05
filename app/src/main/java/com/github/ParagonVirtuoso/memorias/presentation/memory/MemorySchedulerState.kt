package com.github.ParagonVirtuoso.memorias.presentation.memory

sealed class MemorySchedulerState {
    object Initial : MemorySchedulerState()
    object Success : MemorySchedulerState()
    data class Error(val message: String) : MemorySchedulerState()
} 