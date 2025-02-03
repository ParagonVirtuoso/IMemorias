package com.github.ParagonVirtuoso.memorias.presentation.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Welcome(""))
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.value = HomeUiState.Welcome(user.name ?: "")
                } else {
                    _uiState.value = HomeUiState.Error("Usuário não encontrado")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.value = HomeUiState.SignedOut("Logout realizado com sucesso. Todos os dados locais foram limpos.")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Erro ao fazer logout: ${e.message}")
            }
        }
    }

    fun toggleTheme() {
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        val newNightMode = if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(newNightMode)
    }
}

sealed class HomeUiState {
    data class Welcome(val userName: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    data class SignedOut(val message: String) : HomeUiState()
} 