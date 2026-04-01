package com.neonnoir.presentation.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neonnoir.Data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun signUp(name: String, email: String, password: String, confirm: String, termsAccepted: Boolean) {
        when {
            name.isBlank() || email.isBlank() || password.isBlank() ->
                _uiState.value = SignUpUiState.Error("Please fill in all fields")
            password != confirm ->
                _uiState.value = SignUpUiState.Error("Passwords do not match")
            password.length < 6 ->
                _uiState.value = SignUpUiState.Error("Password must be at least 6 characters")
            !termsAccepted ->
                _uiState.value = SignUpUiState.Error("Please accept the Terms of Service")
            else -> viewModelScope.launch {
                _uiState.value = SignUpUiState.Loading
                authRepository.signUpWithEmail(name, email, password)  // ← name, email, password
                    .onSuccess { _uiState.value = SignUpUiState.Success }
                    .onFailure { _uiState.value = SignUpUiState.Error(it.message ?: "Sign up failed") }
            }
        }
    }
}

sealed class SignUpUiState {
    object Idle    : SignUpUiState()
    object Loading : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}