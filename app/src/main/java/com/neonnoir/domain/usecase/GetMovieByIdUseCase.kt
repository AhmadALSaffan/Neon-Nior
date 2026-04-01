package com.neonnoir.domain.usecase

import com.neonnoir.Data.repository.MovieRepository
import com.neonnoir.domain.model.Movie
import com.neonnoir.util.Resource
import javax.inject.Inject

class GetMovieByIdUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    // Delegates fetching of a full movie detail to the repository
    suspend operator fun invoke(imdbId: String): Resource<Movie> =
        repository.getMovieById(imdbId)
}