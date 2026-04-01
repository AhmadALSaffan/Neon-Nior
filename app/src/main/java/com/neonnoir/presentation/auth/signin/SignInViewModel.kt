package com.neonnoir.presentation.auth.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neonnoir.Data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState

    // Attempts Firebase email/password sign in and emits state
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = SignInUiState.Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _uiState.value = SignInUiState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess { _uiState.value = SignInUiState.Success }
                .onFailure { _uiState.value = SignInUiState.Error(it.message ?: "Sign in failed") }
        }
    }
}

sealed class SignInUiState {
    object Idle : SignInUiState()
    object Loading : SignInUiState()
    object Success : SignInUiState()
    data class Error(val message: String) : SignInUiState()
}