package com.neonnoir.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.neonnoir.R
import com.neonnoir.databinding.FragmentLibraryBinding
import com.neonnoir.presentation.common.adapters.ContinueWatchingAdapter
import com.neonnoir.presentation.common.adapters.WatchlistAdapter
import com.neonnoir.util.hide
import com.neonnoir.util.loadCurrentUserAvatar
import com.neonnoir.util.show
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels()

    private lateinit var continueWatchingAdapter: ContinueWatchingAdapter
    private lateinit var watchlistAdapter: WatchlistAdapter

    // Current filter state: "all", "watchlist", "downloads"
    private var activeFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSectionHeaders()
        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupSectionHeaders() {
        with(binding.headerContinue) {
            tvSectionTitle.text = "Continue Watching"
            tvViewAll.text = "View History"
            tvViewAll.show()
        }
        with(binding.headerDownloads) {
            tvSectionTitle.text = "Active Downloads"
        }
        with(binding.headerWatchlist) {
            tvSectionTitle.text = "Your Watchlist"
            tvSectionSubtitle.text = "Movies you've saved"
            tvSectionSubtitle.show()
        }
    }

    private fun setupRecyclerViews() {
        continueWatchingAdapter = ContinueWatchingAdapter { imdbId -> navigateToDetail(imdbId) }
        binding.rvContinue.apply {
            adapter = continueWatchingAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        watchlistAdapter = WatchlistAdapter { imdbId -> navigateToDetail(imdbId) }
        binding.rvWatchlist.apply {
            adapter = watchlistAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun observeViewModel() {
        viewModel.history.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.rvContinue.hide()
                binding.pbContinue.hide()
                binding.emptyContinue.show()
            } else {
                binding.emptyContinue.hide()
                binding.pbContinue.hide()
                binding.rvContinue.show()
                continueWatchingAdapter.submitList(items)
            }
        }

        viewModel.watchlist.observe(viewLifecycleOwner) { items ->
            binding.pbWatchlist.hide()
            if (items.isEmpty()) {
                binding.rvWatchlist.hide()
                binding.emptyWatchlist.show()
            } else {
                binding.emptyWatchlist.hide()
                binding.rvWatchlist.show()
                watchlistAdapter.submitList(items)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnFilterAll.setOnClickListener { applyFilter("all") }
        binding.btnFilterWatchlist.setOnClickListener { applyFilter("watchlist") }
        binding.btnFilterDownloads.setOnClickListener { applyFilter("downloads") }

        binding.btnToolbarSearch.setOnClickListener {
            findNavController().navigate(R.id.nav_search)
        }

        binding.headerContinue.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.nav_search)
        }

        binding.btnDiscoverMore.setOnClickListener {
            findNavController().navigate(R.id.nav_search)
        }
    }

    // Shows/hides sections based on the selected filter tab
    private fun applyFilter(filter: String) {
        activeFilter = filter
        updateFilterButtons()

        when (filter) {
            "all" -> {
                binding.sectionContinue.show()
                binding.sectionDownloads.show()
                binding.sectionWatchlist.show()
            }
            "watchlist" -> {
                binding.sectionContinue.hide()
                binding.sectionDownloads.hide()
                binding.sectionWatchlist.show()
            }
            "downloads" -> {
                binding.sectionContinue.hide()
                binding.sectionDownloads.show()
                binding.sectionWatchlist.hide()
            }
        }
    }

    // Updates filter button backgrounds to reflect the active selection
    private fun updateFilterButtons() {
        val activeRes   = R.drawable.bg_button_primary
        val inactiveRes = R.drawable.bg_button_ghost

        binding.btnFilterAll.setBackgroundResource(
            if (activeFilter == "all") activeRes else inactiveRes
        )
        binding.btnFilterWatchlist.setBackgroundResource(
            if (activeFilter == "watchlist") activeRes else inactiveRes
        )
        binding.btnFilterDownloads.setBackgroundResource(
            if (activeFilter == "downloads") activeRes else inactiveRes
        )

        binding.btnFilterAll.setTextColor(
            requireContext().getColor(
                if (activeFilter == "all") R.color.on_primary else R.color.on_surface_variant
            )
        )
        binding.btnFilterWatchlist.setTextColor(
            requireContext().getColor(
                if (activeFilter == "watchlist") R.color.on_primary else R.color.on_surface_variant
            )
        )
        binding.btnFilterDownloads.setTextColor(
            requireContext().getColor(
                if (activeFilter == "downloads") R.color.on_primary else R.color.on_surface_variant
            )
        )
    }

    private fun navigateToDetail(imdbId: String) {
        val action = LibraryFragmentDirections.actionLibraryToDetail(imdbId)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        binding.ivAvatar.loadCurrentUserAvatar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
