package com.neonnoir.presentation.auth.splash

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    sealed class AuthState {
        object Unknown : AuthState()
        object LoggedIn : AuthState()
        object LoggedOut : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState: StateFlow<AuthState> = _authState

    // Checks Firebase auth state immediately on ViewModel creation
    init {
        checkAuthState()
    }

    // Emits LoggedIn or LoggedOut based on current Firebase user
    private fun checkAuthState() {
        val user = FirebaseAuth.getInstance().currentUser
        _authState.value = if (user != null) AuthState.LoggedIn else AuthState.LoggedOut
    }
}