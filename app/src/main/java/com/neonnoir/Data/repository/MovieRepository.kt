package com.neonnoir.Data.repository

import com.neonnoir.Data.local.entity.WatchlistEntity
import com.neonnoir.domain.model.Movie
import com.neonnoir.domain.model.SearchResult
import com.neonnoir.util.Resource
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    // page = 1 default declared ONCE here in the interface only
    suspend fun searchMovies(query: String, page: Int = 1): Resource<List<SearchResult>>
    suspend fun getMovieById(imdbId: String): Resource<Movie>
    suspend fun getMoviesByIds(ids: List<String>): List<Movie>
    fun getWatchlist(): Flow<List<WatchlistEntity>>
    suspend fun addToWatchlist(movie: Movie)
    suspend fun removeFromWatchlist(imdbId: String)
    suspend fun isInWatchlist(imdbId: String): Boolean
}