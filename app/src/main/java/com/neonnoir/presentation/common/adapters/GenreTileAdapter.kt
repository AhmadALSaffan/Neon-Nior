package com.neonnoir.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neonnoir.databinding.ItemGenreTileBinding
import com.neonnoir.util.loadPoster

data class GenreItem(
    val label: String,       // Display label e.g. "SCI-FI"
    val keyword: String,     // OMDB search keyword e.g. "sci-fi"
    val coverUrl: String = ""// Poster URL loaded from a representative movie
)

class GenreTileAdapter(
    private val onGenreClick: (String) -> Unit
) : ListAdapter<GenreItem, GenreTileAdapter.ViewHolder>(DiffCallback()) {

    // Creates and inflates a new ViewHolder from item_genre_tile.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGenreTileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    // Binds the genre item at the given position to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemGenreTileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Sets genre label, background image, and click listener
        fun bind(item: GenreItem) {
            binding.tvGenreLabel.text = item.label
            if (item.coverUrl.isNotBlank()) {
                binding.ivGenreBg.loadPoster(item.coverUrl)
            }
            binding.root.setOnClickListener { onGenreClick(item.keyword) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GenreItem>() {
        // Checks identity by keyword (unique per genre)
        override fun areItemsTheSame(oldItem: GenreItem, newItem: GenreItem): Boolean =
            oldItem.keyword == newItem.keyword

        // Checks full data equality for efficient partial rebinds
        override fun areContentsTheSame(oldItem: GenreItem, newItem: GenreItem): Boolean =
            oldItem == newItem
    }
}