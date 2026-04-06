package com.neonnoir.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.neonnoir.R
import com.neonnoir.databinding.FragmentProfileBinding
import com.neonnoir.presentation.common.adapters.ContinueWatchingAdapter
import com.neonnoir.util.hide
import com.neonnoir.util.loadCurrentUserAvatar
import com.neonnoir.util.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var continueWatchingAdapter: ContinueWatchingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserInfo()
        setupRecyclerView()
        setupSectionHeader()
        observeViewModel()
        setupClickListeners()
    }

    // Fills in static user details from Firebase Auth
    private fun setupUserInfo() {
        binding.tvDisplayName.text = viewModel.displayName
        binding.tvEmail.text = viewModel.email
    }

    private fun setupSectionHeader() {
        with(binding.headerContinue) {
            tvSectionTitle.text = "Continue Watching"
            tvViewAll.text = "View All →"
            tvViewAll.show()
        }
    }

    private fun setupRecyclerView() {
        continueWatchingAdapter = ContinueWatchingAdapter { imdbId -> navigateToDetail(imdbId) }
        binding.rvContinue.apply {
            adapter = continueWatchingAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.historyCount.observe(viewLifecycleOwner) { count ->
            binding.tvHistoryCount.text = count.toString()
        }

        viewModel.watchlistCount.observe(viewLifecycleOwner) { count ->
            binding.tvWatchlistCount.text = count.toString()
        }

        viewModel.history.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.rvContinue.hide()
                binding.tvEmptyContinue.show()
            } else {
                binding.tvEmptyContinue.hide()
                binding.rvContinue.show()
                continueWatchingAdapter.submitList(items)
            }
        }

        viewModel.signedOut.observe(viewLifecycleOwner) { signedOut ->
            if (signedOut == true) switchToAuthGraph()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_edit)
        }

        binding.rowAccount.setOnClickListener {
            // Account settings placeholder
        }

        binding.rowPreferences.setOnClickListener {
            // Preferences placeholder
        }

        binding.rowHelp.setOnClickListener {
            // Help placeholder
        }

        binding.rowLogout.setOnClickListener {
            viewModel.signOut()
        }

        binding.headerContinue.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.nav_library)
        }
    }

    // Switches the NavController graph back to nav_auth after sign-out
    private fun switchToAuthGraph() {
        val navController = findNavController()
        val graph = navController.navInflater.inflate(R.navigation.nav_auth)
        navController.graph = graph
    }

    private fun navigateToDetail(imdbId: String) {
        val action = ProfileFragmentDirections.actionProfileToDetail(imdbId)
        findNavController().navigate(action)
    }

    // Refreshes the avatar each time the fragment resumes so edits from
    // EditProfileFragment are reflected without re-creating the fragment
    override fun onResume() {
        super.onResume()
        binding.ivAvatar.loadCurrentUserAvatar()
        // Also refresh the display name in case it was updated
        binding.tvDisplayName.text = viewModel.displayName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
