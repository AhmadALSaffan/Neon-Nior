package com.neonnoir.domain.usecase

import com.neonnoir.Data.repository.MovieRepository
import javax.inject.Inject

class RemoveFromWatchlistUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    // Deletes the watchlist entry matching the given IMDB ID
    suspend operator fun invoke(imdbId: String) =
        repository.removeFromWatchlist(imdbId)
}