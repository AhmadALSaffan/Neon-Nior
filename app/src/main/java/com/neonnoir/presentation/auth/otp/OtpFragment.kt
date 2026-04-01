package com.neonnoir.presentation.auth.otp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.neonnoir.R
import com.neonnoir.databinding.FragmentOtpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class OtpFragment : Fragment(R.layout.fragment_otp) {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OtpViewModel by viewModels()
    private val args: OtpFragmentArgs by navArgs()

    // Inflates ViewBinding and returns the root view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up UI and starts all observers
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDescription()
        setupOtpBoxes()
        setupClickListeners()
        observeUiState()
        observeCountdown()
        observeResendAvailability()
    }

    // Builds the description text with the email shown in bold
    private fun setupDescription() {
        binding.tvDescription.text = buildSpannedString {
            append("We've sent a 6-digit premiere code to ")
            bold { append(args.email) }
            append(". Enter it below to unlock your screen.")
        }
    }

    // Wires all 6 OTP boxes with auto-advance and backspace support
    private fun setupOtpBoxes() {
        val boxes = getOtpBoxes()

        boxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < boxes.lastIndex) {
                        // Auto-advance to next box
                        boxes[index + 1].requestFocus()
                    }
                    // Auto-submit when all 6 digits are filled
                    if (index == boxes.lastIndex && s?.length == 1) {
                        val code = boxes.joinToString("") { it.text.toString() }
                        if (code.length == 6) viewModel.verifyOtp(code)
                    }
                }
            })

            // Navigate back to previous box on backspace when current box is empty
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL
                    && event.action == KeyEvent.ACTION_DOWN
                    && editText.text.isEmpty()
                    && index > 0
                ) {
                    boxes[index - 1].requestFocus()
                    boxes[index - 1].text.clear()
                    true
                } else {
                    false
                }
            }
        }

        // Auto-focus the first box on screen open
        boxes[0].requestFocus()
    }

    // Wires BACK, CONFIRM IDENTITY, RESEND NOW, CHANGE EMAIL
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnConfirm.setOnClickListener {
            val code = getEnteredCode()
            viewModel.verifyOtp(code)
        }
        binding.tvResend.setOnClickListener {
            viewModel.resendOtp()
            clearOtpBoxes()
        }
        binding.llChange.setOnClickListener {
            findNavController().popBackStack(R.id.sign_up_fragment, false)
        }
    }

    // Observes OTP verification state: Sending → Idle → Loading → Success / Error
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                when (state) {

                    // Email is being sent via SMTP
                    is OtpUiState.Sending -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnConfirm.isEnabled = false
                        binding.btnConfirm.text = "Sending code..."
                    }

                    // Email sent, waiting for user to enter digits
                    is OtpUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnConfirm.isEnabled = true
                        binding.btnConfirm.text = "CONFIRM IDENTITY"
                    }

                    // OTP digits submitted, verifying
                    is OtpUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnConfirm.isEnabled = false
                        binding.btnConfirm.text = "Verifying..."
                    }

                    // OTP correct — navigate to main app
                    is OtpUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        switchToMainGraph()
                    }

                    // Any error (wrong code, send failure) — show snackbar and reset
                    is OtpUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnConfirm.isEnabled = true
                        binding.btnConfirm.text = "CONFIRM IDENTITY"
                        clearOtpBoxes()
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Observes the countdown timer and updates the label
    private fun observeCountdown() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.countdown.collectLatest { time ->
                binding.tvCountdown.text = time
            }
        }
    }

    // Enables or fades RESEND NOW based on countdown completion
    private fun observeResendAvailability() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.canResend.collectLatest { canResend ->
                binding.tvResend.alpha       = if (canResend) 1f else 0.4f
                binding.tvResend.isClickable = canResend
            }
        }
    }

    // Returns all 6 OTP EditText boxes as an ordered list
    private fun getOtpBoxes(): List<EditText> = listOf(
        binding.etOtp1, binding.etOtp2, binding.etOtp3,
        binding.etOtp4, binding.etOtp5, binding.etOtp6
    )

    // Joins all 6 box values into a single OTP string
    private fun getEnteredCode(): String =
        getOtpBoxes().joinToString("") { it.text.toString() }

    // Clears all boxes and focuses the first one for re-entry
    private fun clearOtpBoxes() {
        getOtpBoxes().forEach { it.text.clear() }
        binding.etOtp1.requestFocus()
    }

    // Swaps the nav graph to nav_main after successful verification
    private fun switchToMainGraph() {
        val navController = findNavController()
        val graph = navController.navInflater.inflate(R.navigation.nav_main)
        navController.graph = graph
    }

    // Clears ViewBinding reference to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}