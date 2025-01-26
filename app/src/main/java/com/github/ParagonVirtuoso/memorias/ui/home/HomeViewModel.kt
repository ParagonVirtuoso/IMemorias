package com.github.ParagonVirtuoso.memorias.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Authenticated(val user: FirebaseUser) : HomeUiState()
    object Unauthenticated : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class HomeEvent {
    object SignInSuccess : HomeEvent()
    object SignOutSuccess : HomeEvent()
    data class ShowMessage(val message: String) : HomeEvent()
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableLiveData<HomeUiState>()
    val uiState: LiveData<HomeUiState> = _uiState

    private val _event = MutableLiveData<HomeEvent>()
    val event: LiveData<HomeEvent> = _event

    fun updateAuthState(user: FirebaseUser?) {
        _uiState.value = when {
            user != null -> HomeUiState.Authenticated(user)
            else -> HomeUiState.Unauthenticated
        }
    }

    fun showError(message: String) {
        _uiState.value = HomeUiState.Error(message)
        _event.value = HomeEvent.ShowMessage(message)
    }

    fun onSignInSuccess() {
        _event.value = HomeEvent.SignInSuccess
    }

    fun onSignOutSuccess() {
        _event.value = HomeEvent.SignOutSuccess
    }
}