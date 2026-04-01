package com.neonnoir.domain.usecase

import com.neonnoir.Data.repository.MovieRepository
import com.neonnoir.domain.model.Movie
import javax.inject.Inject

class AddToWatchlistUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    // Persists the given movie to the local Room watchlist
    suspend operator fun invoke(movie: Movie) =
        repository.addToWatchlist(movie)
}
