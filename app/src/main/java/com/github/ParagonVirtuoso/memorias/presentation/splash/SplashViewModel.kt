package com.github.ParagonVirtuoso.memorias.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val isAuthenticated = authRepository.isUserAuthenticated().first()
                _splashState.value = if (isAuthenticated) {
                    SplashState.Authenticated
                } else {
                    SplashState.Unauthenticated
                }
            } catch (e: Exception) {
                _splashState.value = SplashState.Error(e.message ?: "Erro ao verificar autenticação")
            }
        }
    }
}

sealed class SplashState {
    object Loading : SplashState()
    object Authenticated : SplashState()
    object Unauthenticated : SplashState()
    data class Error(val message: String) : SplashState()
} 