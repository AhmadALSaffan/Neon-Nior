package com.neonnoir.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neonnoir.R
import com.neonnoir.databinding.ItemMoviePortraitBinding
import com.neonnoir.domain.model.SearchResult
import com.neonnoir.util.loadPoster

class SearchResultAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<SearchResult, SearchResultAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMoviePortraitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        // Fill grid cell width and add uniform spacing between items
        val spacing = parent.context.resources
            .getDimensionPixelSize(R.dimen.card_gap)
        binding.root.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(spacing, spacing, spacing, spacing)
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMoviePortraitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchResult) {
            binding.ivPoster.loadPoster(item.poster)
            binding.tvTitle.text = item.title
            binding.tvMeta.text  = buildMeta(item)
            binding.tvRating.text = "—"
            binding.root.setOnClickListener { onItemClick(item.imdbId) }
        }

        // Formats "Movie • 2023" or just "2023" when type is blank
        private fun buildMeta(item: SearchResult): String {
            val type = item.type.replaceFirstChar { it.uppercase() }
            return if (type.isNotBlank()) "$type • ${item.year}" else item.year
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
            oldItem.imdbId == newItem.imdbId

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
            oldItem == newItem
    }
}
