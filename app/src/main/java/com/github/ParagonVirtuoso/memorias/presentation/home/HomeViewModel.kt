package com.github.ParagonVirtuoso.memorias.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import com.github.ParagonVirtuoso.memorias.util.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Initial)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        getCurrentUser()
        // Aplicar o tema salvo ao iniciar
        themePreferences.applyTheme(themePreferences.isDarkMode())
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            try {
                authRepository.getCurrentUser().collect { user ->
                    if (user != null) {
                        _uiState.value = HomeUiState.Welcome(user.name ?: "")
                    } else {
                        _uiState.value = HomeUiState.Error("Usuário não encontrado")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Erro ao carregar usuário: ${e.message}")
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
        val newDarkMode = !themePreferences.isDarkMode()
        themePreferences.setDarkMode(newDarkMode)
    }
}

sealed class HomeUiState {
    data class Welcome(val userName: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    data class SignedOut(val message: String) : HomeUiState()
    object Initial : HomeUiState()
} 