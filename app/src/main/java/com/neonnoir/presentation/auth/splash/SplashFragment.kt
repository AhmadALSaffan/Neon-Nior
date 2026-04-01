package com.neonnoir.presentation.auth.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neonnoir.R
import com.neonnoir.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()

    // Inflates ViewBinding and returns the root view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up click listeners and observes auth state
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeAuthState()
    }

    // Navigates to SignUp when GET STARTED is tapped
    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_splash_to_signUp)
        }
        binding.btnSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_splash_to_signIn)
        }
    }

    // Observes auth state and switches to main graph if already logged in
    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is SplashViewModel.AuthState.LoggedIn  -> switchToMainGraph()
                    is SplashViewModel.AuthState.LoggedOut -> Unit
                    is SplashViewModel.AuthState.Unknown   -> Unit
                }
            }
        }
    }

    // Replaces the nav graph with nav_main to enter the main app flow
    private fun switchToMainGraph() {
        val navController = findNavController()
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_main)
        navController.graph = graph
    }

    // Clears ViewBinding reference to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}