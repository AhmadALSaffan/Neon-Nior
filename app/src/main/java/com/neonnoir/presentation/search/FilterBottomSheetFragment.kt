package com.neonnoir.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.neonnoir.databinding.FragmentFilterBottomSheetBinding

data class FilterOptions(
    val type: String?,   // "movie", "series", or null (all)
    val year: String?,   // 4-digit year string or null
    val genre: String?   // genre keyword or null
)

class FilterBottomSheetFragment(
    private val currentFilters: FilterOptions,
    private val onFiltersApplied: (FilterOptions) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreFilters()
        binding.btnApplyFilters.setOnClickListener { applyAndDismiss() }
    }

    // Pre-selects radio buttons and chips from the current filter state
    private fun restoreFilters() {
        when (currentFilters.type) {
            "movie"  -> binding.rbTypeMovie.isChecked = true
            "series" -> binding.rbTypeSeries.isChecked = true
            else     -> binding.rbTypeAll.isChecked = true
        }

        binding.etYear.setText(currentFilters.year ?: "")

        val genreChip = when (currentFilters.genre) {
            "sci-fi"    -> binding.chipGenreScifi
            "horror"    -> binding.chipGenreHorror
            "drama"     -> binding.chipGenreDrama
            "action"    -> binding.chipGenreAction
            "thriller"  -> binding.chipGenreThriller
            "noir"      -> binding.chipGenreNoir
            else        -> binding.chipGenreAll
        }
        genreChip.isChecked = true
    }

    // Reads current UI selections and returns them via the callback
    private fun applyAndDismiss() {
        val type = when (binding.rgType.checkedRadioButtonId) {
            binding.rbTypeMovie.id  -> "movie"
            binding.rbTypeSeries.id -> "series"
            else                    -> null
        }

        val year = binding.etYear.text?.toString()?.trim()?.takeIf { it.length == 4 }

        val genre = when (binding.chipGroupGenre.checkedChipId) {
            binding.chipGenreScifi.id    -> "sci-fi"
            binding.chipGenreHorror.id   -> "horror"
            binding.chipGenreDrama.id    -> "drama"
            binding.chipGenreAction.id   -> "action"
            binding.chipGenreThriller.id -> "thriller"
            binding.chipGenreNoir.id     -> "noir"
            else                         -> null
        }

        onFiltersApplied(FilterOptions(type = type, year = year, genre = genre))
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FilterBottomSheet"
    }
}
