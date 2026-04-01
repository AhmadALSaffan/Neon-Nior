package com.neonnoir.presentation.auth.otp

import android.os.CountDownTimer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neonnoir.util.EmailOtpSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Email passed from SignUpFragment via Safe Args
    private val email: String = savedStateHandle["email"] ?: ""

    private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val uiState: StateFlow<OtpUiState> = _uiState

    private val _countdown = MutableStateFlow("01:00")
    val countdown: StateFlow<String> = _countdown

    private val _canResend = MutableStateFlow(false)
    val canResend: StateFlow<Boolean> = _canResend

    // Holds the currently active OTP code
    private var generatedCode: String = ""
    private var timer: CountDownTimer? = null

    // Sends the first OTP as soon as the ViewModel is created
    init {
        generateAndSendOtp()
    }

    // Generates a 6-digit code, sends it via SMTP, starts the countdown
    private fun generateAndSendOtp() {
        generatedCode = Random.nextInt(100000, 999999).toString()
        _uiState.value = OtpUiState.Sending

        viewModelScope.launch {
            val result = EmailOtpSender.sendOtp(email, generatedCode)
            if (result.isSuccess) {
                _uiState.value = OtpUiState.Idle
                startCountdown()
            } else {
                _uiState.value = OtpUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to send email"
                )
            }
        }
    }

    // Counts down 60 seconds then unlocks RESEND NOW
    private fun startCountdown() {
        _canResend.value = false
        timer?.cancel()
        timer = object : CountDownTimer(60_000L, 1_000L) {
            override fun onTick(millisRemaining: Long) {
                val secs = millisRemaining / 1000
                _countdown.value = "00:%02d".format(secs)
            }
            override fun onFinish() {
                _countdown.value = "00:00"
                _canResend.value = true
            }
        }.start()
    }

    // Checks entered OTP against the generated code
    fun verifyOtp(entered: String) {
        if (entered.length < 6) {
            _uiState.value = OtpUiState.Error("Enter all 6 digits")
            return
        }
        if (entered == generatedCode) {
            _uiState.value = OtpUiState.Success
        } else {
            _uiState.value = OtpUiState.Error("Incorrect code. Please try again.")
        }
    }

    // Re-generates and re-sends OTP when user taps RESEND NOW
    fun resendOtp() {
        if (!_canResend.value) return
        generateAndSendOtp()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}

sealed class OtpUiState {
    object Idle    : OtpUiState()
    object Sending : OtpUiState()    // while email is being sent
    object Loading : OtpUiState()    // while verifying
    object Success : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}