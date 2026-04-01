package com.neonnoir.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neonnoir.databinding.ItemRelatedMovieBinding
import com.neonnoir.domain.model.SearchResult
import com.neonnoir.util.loadPoster

class RelatedMoviesAdapter(
    private val onMovieClick: (String) -> Unit
) : ListAdapter<SearchResult, RelatedMoviesAdapter.ViewHolder>(DiffCallback()) {

    // Creates and inflates a new ViewHolder from item_related_movie.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRelatedMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    // Binds the SearchResult at the given position to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemRelatedMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Loads poster, sets title and meta, attaches click listener
        fun bind(item: SearchResult) {
            binding.ivPoster.loadPoster(item.poster)
            binding.tvTitle.text = item.title
            binding.tvMeta.text  = buildMeta(item)
            binding.root.setOnClickListener { onMovieClick(item.imdbId) }
        }

        // Formats "Type • Year" for the meta line below the title
        private fun buildMeta(item: SearchResult): String {
            val type = item.type.replaceFirstChar { it.uppercaseChar() }
            return "$type • ${item.year}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        // Compares identity by IMDB ID
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
            oldItem.imdbId == newItem.imdbId

        // Compares full data equality for partial rebind optimisation
        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
            oldItem == newItem
    }
}