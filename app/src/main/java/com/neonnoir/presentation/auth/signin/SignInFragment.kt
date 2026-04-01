package com.neonnoir.presentation.auth.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.neonnoir.R
import com.neonnoir.databinding.FragmentSignInBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignInViewModel by viewModels()

    // Inflates ViewBinding and returns the root view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up click listeners and starts observing UI state
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUiState()
    }

    // Wires all button and link click actions
    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.signIn(email, password)
        }
        binding.tvSignUpLink.setOnClickListener {
            findNavController().navigate(R.id.action_signIn_to_signUp)
        }
        binding.tvForgot.setOnClickListener {
            findNavController().navigate(R.id.action_signIn_to_forgot)
        }
    }

    // Observes ViewModel state and updates UI accordingly
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is SignInUiState.Loading -> showLoading(true)
                    is SignInUiState.Success -> switchToMainGraph()
                    is SignInUiState.Error   -> {
                        showLoading(false)
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    is SignInUiState.Idle -> showLoading(false)
                }
            }
        }
    }

    // Shows or hides the loading state on the sign in button
    private fun showLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        binding.btnSignIn.text = if (isLoading) "Signing in..." else "Sign In →"
    }

    // Replaces nav graph with nav_main to enter the main app
    private fun switchToMainGraph() {
        val navController = findNavController()
        val graph = navController.navInflater.inflate(R.navigation.nav_main)
        navController.graph = graph
    }

    // Clears ViewBinding to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}