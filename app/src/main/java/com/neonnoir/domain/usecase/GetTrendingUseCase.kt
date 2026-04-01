package com.neonnoir.domain.usecase

import com.neonnoir.Data.repository.MovieRepository
import com.neonnoir.domain.model.Movie
import javax.inject.Inject

class GetTrendingUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    // Fetches full movie details for each ID in the supplied curated list
    suspend operator fun invoke(ids: List<String>): List<Movie> =
        repository.getMoviesByIds(ids)
}