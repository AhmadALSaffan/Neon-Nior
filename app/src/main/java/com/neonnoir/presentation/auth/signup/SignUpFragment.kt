package com.neonnoir.presentation.auth.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neonnoir.R
import com.neonnoir.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels()

    // Inflates ViewBinding and returns the root view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up click listeners and starts observing UI state
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUiState()
    }

    // Wires all button click actions
    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            viewModel.signUp(
                name          = binding.etFullName.text.toString().trim(),
                email         = binding.etEmail.text.toString().trim(),
                password      = binding.etPassword.text.toString().trim(),
                confirm       = binding.etConfirm.text.toString().trim(),
                termsAccepted = binding.cbTerms.isChecked
            )
        }
        binding.btnSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUp_to_signIn)
        }
    }

    // Observes ViewModel state and updates UI accordingly
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is SignUpUiState.Loading -> showLoading(true)
                    is SignUpUiState.Success -> {
                        showLoading(false)
                        val email = binding.etEmail.text.toString().trim()
                        navigateToOtp(email)
                    }
                    is SignUpUiState.Error -> {
                        showLoading(false)
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = state.message
                    }
                    is SignUpUiState.Idle -> showLoading(false)
                }
            }
        }
    }

    // Navigates to OTP screen passing the registered email as argument
    private fun navigateToOtp(email: String) {
        val action = SignUpFragmentDirections.actionSignUpToOtp(email)
        findNavController().navigate(action)
    }

    // Shows or hides loading state on the sign up button
    private fun showLoading(isLoading: Boolean) {
        binding.btnSignUp.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignUp.text = if (isLoading) "Creating account..." else "SIGN UP →"
        binding.tvError.visibility = View.GONE
    }

    // Clears ViewBinding to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}