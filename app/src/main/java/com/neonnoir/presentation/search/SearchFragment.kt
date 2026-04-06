package com.neonnoir.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.neonnoir.R
import com.neonnoir.databinding.FragmentSearchBinding
import com.neonnoir.presentation.common.adapters.GenreTileAdapter
import com.neonnoir.presentation.common.adapters.SearchResultAdapter
import com.neonnoir.util.Resource
import com.neonnoir.util.hide
import com.neonnoir.util.loadCurrentUserAvatar
import com.neonnoir.util.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private val args: SearchFragmentArgs by navArgs()

    private lateinit var resultsAdapter: SearchResultAdapter
    private lateinit var popularAdapter: SearchResultAdapter
    private lateinit var genreAdapter: GenreTileAdapter

    private var activeFilters = FilterOptions(type = null, year = null, genre = null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupSearchBar()
        setupClickListeners()
        observeViewModel()

        val initialKeyword = args.keyword
        if (initialKeyword.isNotBlank()) {
            binding.etSearch.setText(initialKeyword)
            binding.etSearch.setSelection(initialKeyword.length)
            viewModel.setQuery(initialKeyword)
            showSearchState()
        }

        binding.root.setOnTouchListener { _, _ ->
            hideKeyboard()
            true
        }
    }

    private fun setupRecyclerViews() {
        resultsAdapter = SearchResultAdapter { imdbId -> navigateToDetail(imdbId) }
        binding.rvResults.apply {
            adapter       = resultsAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        popularAdapter = SearchResultAdapter { imdbId -> navigateToDetail(imdbId) }
        binding.rvPopular.apply {
            adapter       = popularAdapter
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        }

        genreAdapter = GenreTileAdapter { keyword -> fillSearchFromGenre(keyword) }
        binding.rvGenres.apply {
            adapter                  = genreAdapter
            layoutManager            = GridLayoutManager(requireContext(), 2)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.setQuery(query)
                if (query.isBlank()) showDefaultState() else showSearchState()
            }
        })

        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text?.toString()?.trim() ?: ""
                if (query.isNotBlank()) viewModel.saveRecentSearch(query)
                hideKeyboard()
                v.clearFocus()
                true
            } else false
        }
    }

    private fun setupClickListeners() {
        binding.tvClearAll.setOnClickListener {
            viewModel.clearRecentSearches()
            binding.llRecentChips.removeAllViews()
        }

        binding.btnFilter.setOnClickListener {
            FilterBottomSheetFragment(
                currentFilters = activeFilters,
                onFiltersApplied = { filters ->
                    activeFilters = filters
                    val q = binding.etSearch.text?.toString()?.trim() ?: ""
                    if (q.isNotBlank()) viewModel.setQuery(q)
                }
            ).show(childFragmentManager, FilterBottomSheetFragment.TAG)
        }
    }

    private fun observeViewModel() {

        // ── Search results ──────────────────────────────────────────────────
        viewModel.results.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pbLoading.show()
                    binding.rvResults.hide()
                }
                is Resource.Success -> {
                    binding.pbLoading.hide()
                    binding.rvResults.show()
                    resultsAdapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    binding.pbLoading.hide()
                    binding.rvResults.show()
                    resultsAdapter.submitList(emptyList())
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // ── Popular for You ─────────────────────────────────────────────────
        viewModel.popularMovies.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pbPopular.show()
                    binding.rvPopular.hide()
                }
                is Resource.Success -> {
                    binding.pbPopular.hide()
                    binding.rvPopular.show()
                    popularAdapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    binding.pbPopular.hide()
                    binding.rvPopular.show()
                }
            }
        }

        // ── Explore Genres ──────────────────────────────────────────────────
        viewModel.genreItems.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                binding.pbGenres.hide()
                binding.rvGenres.show()
                genreAdapter.submitList(items)
            }
        }

        // ── Recent search chips ─────────────────────────────────────────────
        viewModel.recentSearches.observe(viewLifecycleOwner) { searches ->
            rebuildRecentChips(searches)
        }
    }

    private fun rebuildRecentChips(searches: List<String>) {
        binding.llRecentChips.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        searches.forEach { query ->
            val chip = inflater.inflate(
                R.layout.item_recent_chip, binding.llRecentChips, false
            ) as TextView
            chip.text = "↺  $query"
            chip.setOnClickListener {
                binding.etSearch.setText(query)
                binding.etSearch.setSelection(query.length)
                viewModel.setQuery(query)
            }
            binding.llRecentChips.addView(chip)
        }
    }

    private fun fillSearchFromGenre(keyword: String) {
        binding.etSearch.setText(keyword)
        binding.etSearch.setSelection(keyword.length)
        viewModel.setQuery(keyword)
    }

    // ── Visibility helpers ──────────────────────────────────────────────────

    /** Default state: hide results panel, show default scroll content */
    private fun showDefaultState() {
        binding.llDefaultState.show()
        binding.rvResults.hide()
        binding.pbLoading.hide()
    }

    /** Search active: hide default state, show results area (spinner or data) */
    private fun showSearchState() {
        binding.llDefaultState.hide()
        // pb_loading / rv_results toggled by results LiveData observer
    }

    // ── Keyboard ────────────────────────────────────────────────────────────

    private fun hideKeyboard() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        binding.root.requestFocus()
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    private fun navigateToDetail(imdbId: String) {
        val action = SearchFragmentDirections.actionSearchToDetail(imdbId)
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
