package com.neonnoir.util

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.neonnoir.R

// Loads a portrait poster URL with crossfade and dark placeholder
fun ImageView.loadPoster(url: String) {
    Glide.with(this)
        .load(url.ifBlank { null })
        .placeholder(R.drawable.placeholder_poster)
        .error(R.drawable.placeholder_poster)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .into(this)
}

// Loads a wide backdrop URL with a slower fade-in for hero sections
fun ImageView.loadBackdrop(url: String) {
    Glide.with(this)
        .load(url.ifBlank { null })
        .placeholder(R.drawable.placeholder_backdrop)
        .error(R.drawable.placeholder_backdrop)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade(350))
        .into(this)
}

// Sets visibility to VISIBLE
fun View.show() { visibility = View.VISIBLE }

// Sets visibility to GONE
fun View.hide() { visibility = View.GONE }

// Makes this view visible and hides the other view
fun View.showWhileHiding(other: View) {
    visibility = View.VISIBLE
    other.visibility = View.GONE
}

// Hides this view and makes the other view visible
fun View.hideWhileShowing(other: View) {
    visibility = View.GONE
    other.visibility = View.VISIBLE
}

// Converts a dp value to pixels using this view's display metrics
fun View.dpToPx(dp: Int): Int =
    (dp * resources.displayMetrics.density).toInt()

/**
 * Loads the currently signed-in Firebase user's avatar photo into this ImageView.
 * Uses circleCrop so it works for both small toolbar avatars and large profile avatars.
 * Falls back to [R.drawable.ic_person] if no photo is set.
 * Call in onResume() so the image always refreshes after an edit.
 */
fun ImageView.loadCurrentUserAvatar() {
    val photoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
    Glide.with(this)
        .load(photoUrl.takeIf { !it.isNullOrBlank() })
        .placeholder(R.drawable.ic_person)
        .error(R.drawable.ic_person)
        .circleCrop()
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .into(this)
}