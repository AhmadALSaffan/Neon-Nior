package com.neonnoir.presentation.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.neonnoir.R
import com.neonnoir.databinding.FragmentDetailBinding
import com.neonnoir.domain.model.Movie
import com.neonnoir.presentation.common.adapters.CastAdapter
import com.neonnoir.presentation.common.adapters.RelatedMoviesAdapter
import com.neonnoir.util.hide
import com.neonnoir.util.loadBackdrop
import com.neonnoir.util.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail) {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()

    private lateinit var castAdapter: CastAdapter
    private lateinit var relatedAdapter: RelatedMoviesAdapter

    // Inflates ViewBinding and returns root view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Triggers data load, sets up adapters, observers, and click listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
        viewModel.load(args.imdbId)
    }

    // Configures the cast list and related movies grid with their adapters
    private fun setupRecyclerViews() {
        castAdapter = CastAdapter()
        binding.rvCast.apply {
            adapter      = castAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        relatedAdapter = RelatedMoviesAdapter { imdbId -> navigateToDetail(imdbId) }
        binding.rvRelated.apply {
            adapter      = relatedAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            isNestedScrollingEnabled = false
        }
    }

    // Observes uiState, watchlist state, and related movies from the ViewModel
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DetailUiState.Loading -> showLoading()
                is DetailUiState.Success -> showContent(state.movie)
                is DetailUiState.Error   -> showError(state.message)
            }
        }

        viewModel.isInWatchlist.observe(viewLifecycleOwner) { inList ->
            updateWatchlistButton(inList)
        }

        viewModel.relatedMovies.observe(viewLifecycleOwner) { results ->
            relatedAdapter.submitList(results)
        }
    }

    // Attaches click listeners to back, share, play, and watchlist buttons
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnShare.setOnClickListener {
            val movie = (viewModel.uiState.value as? DetailUiState.Success)?.movie
            movie?.let { shareMovie(it) }
        }
        binding.btnPlay.setOnClickListener {
            Snackbar.make(binding.root, "Playback not available in demo", Snackbar.LENGTH_SHORT).show()
        }
        binding.btnWatchlist.setOnClickListener {
            val movie = (viewModel.uiState.value as? DetailUiState.Success)?.movie
            movie?.let { viewModel.toggleWatchlist(it) }
        }
    }

    // Hides all content sections while the data is loading
    private fun showLoading() {
        binding.nestedScroll.hide()
    }

    // Populates every UI field with data from the loaded Movie domain model
    private fun showContent(movie: Movie) {
        binding.nestedScroll.show()

        binding.ivBackdrop.loadBackdrop(movie.poster)
        binding.tvTitle.text      = movie.title.uppercase()
        binding.tvPlot.text       = movie.plot
        binding.tvDirector.text   = movie.director
        binding.tvStudio.text     = movie.rated.ifBlank { "—" }

        binding.tvGenreTag.text   = movie.genres.firstOrNull()?.uppercase() ?: ""
        binding.tvRuntimeTag.text = movie.runtime.uppercase()
        binding.tvYearTag.text    = movie.year
        binding.tvRated.text      = movie.rated.takeIf { it != "N/A" } ?: ""
        binding.tvRated.visibility = if (movie.rated == "N/A") View.GONE else View.VISIBLE

        binding.tvRating.text = if (movie.imdbRating > 0f)
            String.format("%.1f", movie.imdbRating)
        else
            "—"

        castAdapter.submitActors(movie.actors.joinToString(", "))
        buildGenreChips(movie.genres)
    }

    // Dynamically inflates a Chip for each genre keyword
    private fun buildGenreChips(genres: List<String>) {
        binding.chipGroupGenres.removeAllViews()
        genres.forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text              = genre
                isCheckable       = false
                chipBackgroundColor = resources.getColorStateList(
                    R.color.surface_container_highest, requireContext().theme
                )
                setTextColor(resources.getColor(R.color.on_surface_variant, requireContext().theme))
                chipStrokeWidth = 1f
                chipStrokeColor = resources.getColorStateList(
                    R.color.outline_variant, requireContext().theme
                )
            }
            binding.chipGroupGenres.addView(chip)
        }
    }

    // Updates the watchlist button icon and text to reflect saved vs unsaved state
    private fun updateWatchlistButton(inList: Boolean) {
        if (inList) {
            binding.btnWatchlist.text = "✓ IN WATCHLIST"
            binding.btnWatchlist.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_bookmark, 0, 0, 0
            )
        } else {
            binding.btnWatchlist.text = "+ WATCHLIST"
            binding.btnWatchlist.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_bookmark_outline, 0, 0, 0
            )
        }
    }

    // Shows a Snackbar with the given error message
    private fun showError(message: String) {
        binding.nestedScroll.show()
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // Launches a system share sheet with the movie title
    private fun shareMovie(movie: Movie) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out ${movie.title} on Neon Noir!")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    // Navigates to another movie's detail screen, replacing the current back-stack entry
    private fun navigateToDetail(imdbId: String) {
        val action = DetailFragmentDirections.actionDetailSelf(imdbId)
        findNavController().navigate(action)
    }

    // Nulls out the binding reference to prevent memory leaks after view destruction
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}