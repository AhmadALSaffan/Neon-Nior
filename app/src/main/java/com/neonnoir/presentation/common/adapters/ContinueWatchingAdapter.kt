package com.neonnoir.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neonnoir.Data.local.entity.HistoryEntity
import com.neonnoir.databinding.ItemMovieLandscapeBinding
import com.neonnoir.util.loadBackdrop

class ContinueWatchingAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<HistoryEntity, ContinueWatchingAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMovieLandscapeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMovieLandscapeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entity: HistoryEntity) {
            binding.ivPoster.loadBackdrop(entity.poster)
            binding.tvTitle.text = entity.title
            binding.pbProgress.progress = 0
            binding.root.setOnClickListener { onItemClick(entity.imdbId) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryEntity>() {
        override fun areItemsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity) =
            oldItem.imdbId == newItem.imdbId

        override fun areContentsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity) =
            oldItem == newItem
    }
}
