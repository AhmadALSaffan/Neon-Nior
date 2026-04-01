package com.neonnoir.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.neonnoir.R
import com.neonnoir.presentation.home.HomeFragmentDirections
import com.neonnoir.databinding.FragmentHomeBinding
import com.neonnoir.domain.model.Movie
import com.neonnoir.presentation.common.adapters.GenreTileAdapter
import com.neonnoir.presentation.common.adapters.MovieCardAdapter
import com.neonnoir.presentation.home.HomeViewModel
import com.neonnoir.util.Resource
import com.neonnoir.util.hide
import com.neonnoir.util.loadBackdrop
import com.neonnoir.util.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var trendingAdapter: MovieCardAdapter
    private lateinit var recentAdapter: MovieCardAdapter
    private lateinit var genreAdapter: GenreTileAdapter

    // Inflates ViewBinding and returns the root view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up UI, adapters, observers, and click listeners after view creation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSectionHeaders()
        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
    }

    // Configures section header texts, subtitles, and VIEW ALL visibility
    private fun setupSectionHeaders() {
        with(binding.headerTrending) {
            tvSectionTitle.text    = "Trending Now"
            tvSectionSubtitle.text = "Most watched in your region"
            tvSectionSubtitle.show()
            tvViewAll.show()
        }
        with(binding.headerGenres) {
            tvSectionTitle.text    = "Top Genres"
            tvSectionSubtitle.text = "Curation based on your mood"
            tvSectionSubtitle.show()
        }
        with(binding.headerRecent) {
            tvSectionTitle.text    = "Recently Added"
            tvSectionSubtitle.text = "New arrivals this week"
            tvSectionSubtitle.show()
            tvViewAll.show()
        }
    }

    // Initialises the three RecyclerViews with their adapters and layout managers
    private fun setupRecyclerViews() {
        trendingAdapter = MovieCardAdapter { imdbId -> navigateToDetail(imdbId) }
        binding.rvTrending.apply {
            adapter            = trendingAdapter
            layoutManager      = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            setHasFixedSize(true)
        }

        recentAdapter = MovieCardAdapter { imdbId -> navigateToDetail(imdbId) }
        binding.rvRecent.apply {
            adapter            = recentAdapter
            layoutManager      = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            setHasFixedSize(true)
        }

        genreAdapter = GenreTileAdapter { keyword -> navigateToSearch(keyword) }
        binding.rvGenres.apply {
            adapter            = genreAdapter
            layoutManager      = GridLayoutManager(requireContext(), 2)
            isNestedScrollingEnabled = false
        }
    }

    // Observes all LiveData from HomeViewModel and updates the UI accordingly
    private fun observeViewModel() {
        viewModel.heroMovie.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    bindHero(resource.data)
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message)
                }
            }
        }

        viewModel.trendingMovies.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                trendingAdapter.submitList(resource.data)
            }
        }

        viewModel.recentMovies.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                recentAdapter.submitList(resource.data)
            }
        }

        viewModel.genreItems.observe(viewLifecycleOwner) { items ->
            genreAdapter.submitList(items)
        }
    }

    // Attaches click listeners to toolbar icons and hero action buttons
    private fun setupClickListeners() {
        binding.btnToolbarSearch.setOnClickListener {
            findNavController().navigate(R.id.nav_search)
        }
        binding.btnMenu.setOnClickListener {
            // Drawer or bottom sheet — wire up once DrawerLayout is added
        }
        binding.ivAvatar.setOnClickListener {
            findNavController().navigate(R.id.nav_profile)
        }
        binding.heroSection.btnHeroWatchNow.setOnClickListener {
            val currentHero = (viewModel.heroMovie.value as? Resource.Success)?.data
            currentHero?.let { navigateToDetail(it.imdbId) }
        }
        binding.heroSection.btnHeroWatchlist.setOnClickListener {
            // Watchlist toggle wired in DetailFragment — hero button navigates to detail
            val currentHero = (viewModel.heroMovie.value as? Resource.Success)?.data
            currentHero?.let { navigateToDetail(it.imdbId) }
        }
        binding.headerTrending.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.nav_search)
        }
        binding.headerRecent.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.nav_search)
        }
    }

    // Fills the hero section views with the featured Movie's data
    private fun bindHero(movie: Movie) {
        with(binding.heroSection) {
            ivHeroBackdrop.loadBackdrop(movie.poster)
            tvHeroTitle.text = movie.title
            tvHeroPlot.text  = movie.plot
            tvHeroMeta.text  = buildHeroMeta(movie)
        }
    }

    // Builds the meta string: "Genre • Runtime • Year" shown in the hero badge row
    private fun buildHeroMeta(movie: Movie): String {
        val genre   = movie.genres.firstOrNull() ?: ""
        val runtime = movie.runtime
        val year    = movie.year
        return listOf(genre, runtime, year)
            .filter { it.isNotBlank() && it != "N/A" }
            .joinToString(" • ")
    }

    // Shows or hides the full-screen loading overlay
    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.isVisible = isLoading
    }

    // Shows a Snackbar with the given error message
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // Navigates to the detail screen passing the selected IMDB ID as a Safe Args argument
    private fun navigateToDetail(imdbId: String) {
        val action = HomeFragmentDirections.actionHomeToDetail(imdbId)
        findNavController().navigate(action)
    }

    // Navigates to the search screen with a pre-filled keyword for genre browsing
    private fun navigateToSearch(keyword: String) {
        val action = HomeFragmentDirections.actionHomeToSearch(keyword)
        findNavController().navigate(action)
    }

    // Nulls out the binding reference to prevent memory leaks after view destruction
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}