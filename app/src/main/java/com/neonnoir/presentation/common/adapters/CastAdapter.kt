package com.neonnoir.presentation.common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neonnoir.databinding.ItemCastMemberBinding

class CastAdapter : RecyclerView.Adapter<CastAdapter.ViewHolder>() {

    private var actors: List<String> = emptyList()

    // Replaces the full actor list and redraws all items
    fun submitActors(rawActors: String) {
        actors = rawActors
            .split(", ")
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "N/A" }
        notifyDataSetChanged()
    }

    // Creates and inflates a new ViewHolder from item_cast_member.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCastMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    // Binds the actor name and derived initials to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(actors[position])
    }

    // Returns the total number of cast members
    override fun getItemCount(): Int = actors.size

    inner class ViewHolder(
        private val binding: ItemCastMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Sets the actor name, extracts up-to-2 initials, clears the character field
        fun bind(fullName: String) {
            binding.tvActorName.text = fullName
            binding.tvInitials.text  = buildInitials(fullName)
            binding.tvCharacter.text = ""
        }

        // Takes the first letter of each word, max two, and uppercases them
        private fun buildInitials(name: String): String =
            name.split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
                .take(2)
                .joinToString("")
    }
}