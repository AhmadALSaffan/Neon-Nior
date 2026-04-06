package com.neonnoir.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.neonnoir.Data.local.entity.HistoryEntity
import com.neonnoir.Data.repository.AuthRepository
import com.neonnoir.domain.usecase.GetHistoryUseCase
import com.neonnoir.domain.usecase.GetWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    getWatchlistUseCase: GetWatchlistUseCase,
    getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    // User info from Firebase Auth
    private val currentUser get() = FirebaseAuth.getInstance().currentUser
    val displayName: String get() = currentUser?.displayName?.ifBlank { null } ?: "Guest"
    val email: String get() = currentUser?.email ?: ""

    // Watchlist count from Room — updates reactively
    val watchlistCount: LiveData<Int> = getWatchlistUseCase()
        .asLiveData()
        .map { it.size }

    // History items for Continue Watching carousel
    val history: LiveData<List<HistoryEntity>> = getHistoryUseCase().asLiveData()

    // History count derived from history items
    val historyCount: LiveData<Int> = history.map { it.size }

    // Emits true once sign-out is complete so the Fragment can switch graph
    private val _signedOut = MutableLiveData<Boolean>()
    val signedOut: LiveData<Boolean> = _signedOut

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _signedOut.value = true
        }
    }
}
