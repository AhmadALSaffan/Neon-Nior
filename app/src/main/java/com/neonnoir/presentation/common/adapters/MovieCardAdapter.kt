package com.neonnoir.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neonnoir.databinding.ItemMoviePortraitBinding
import com.neonnoir.domain.model.Movie
import com.neonnoir.util.loadPoster

class MovieCardAdapter(
    private val onMovieClick: (String) -> Unit
) : ListAdapter<Movie, MovieCardAdapter.ViewHolder>(DiffCallback()) {

    // Creates and inflates a new ViewHolder from item_movie_portrait.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMoviePortraitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    // Binds the movie at the given position to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMoviePortraitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Populates poster, rating, title, and meta from the Movie domain model
        fun bind(movie: Movie) {
            binding.ivPoster.loadPoster(movie.poster)
            binding.tvTitle.text = movie.title
            binding.tvMeta.text = buildMeta(movie)
            binding.tvRating.text = if (movie.imdbRating > 0f)
                String.format("%.1f", movie.imdbRating)
            else
                "—"
            binding.root.setOnClickListener { onMovieClick(movie.imdbId) }
        }

        // Formats the genre + year meta string shown below the title
        private fun buildMeta(movie: Movie): String {
            val genre = movie.genres.firstOrNull() ?: ""
            return if (genre.isNotBlank()) "$genre • ${movie.year}" else movie.year
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Movie>() {
        // Checks identity by IMDB ID
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean =
            oldItem.imdbId == newItem.imdbId

        // Checks full data equality for efficient partial rebinds
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean =
            oldItem == newItem
    }
}