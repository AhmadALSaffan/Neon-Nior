package com.neonnoir.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neonnoir.Data.local.entity.WatchlistEntity
import com.neonnoir.databinding.ItemMoviePortraitBinding
import com.neonnoir.util.loadPoster

class WatchlistAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<WatchlistEntity, WatchlistAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMoviePortraitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMoviePortraitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entity: WatchlistEntity) {
            binding.ivPoster.loadPoster(entity.poster)
            binding.tvTitle.text = entity.title
            binding.tvMeta.text = if (entity.genre.isNotBlank()) "${entity.genre} • ${entity.year}"
                                  else entity.year
            binding.tvRating.text = if (entity.rating > 0f)
                String.format("%.1f", entity.rating)
            else "—"
            binding.root.setOnClickListener { onItemClick(entity.imdbId) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WatchlistEntity>() {
        override fun areItemsTheSame(oldItem: WatchlistEntity, newItem: WatchlistEntity) =
            oldItem.imdbId == newItem.imdbId

        override fun areContentsTheSame(oldItem: WatchlistEntity, newItem: WatchlistEntity) =
            oldItem == newItem
    }
}
