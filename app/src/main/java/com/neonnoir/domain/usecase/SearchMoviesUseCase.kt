package com.neonnoir.domain.usecase

import com.neonnoir.Data.repository.MovieRepository
import com.neonnoir.domain.model.SearchResult
import com.neonnoir.util.Resource
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(query: String, page: Int = 1): Resource<List<SearchResult>> =
        repository.searchMovies(query, page)
}
