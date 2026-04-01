package com.neonnoir.Data.repository

import com.neonnoir.Data.local.dao.HistoryDao
import com.neonnoir.Data.local.dao.WatchlistDao
import com.neonnoir.Data.local.entity.WatchlistEntity
import com.neonnoir.Data.mapper.MovieMapper.toDomain
import com.neonnoir.Data.remote.api.OmdbApiService
import com.neonnoir.domain.model.Movie
import com.neonnoir.domain.model.SearchResult
import com.neonnoir.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val api: OmdbApiService,
    private val watchlistDao: WatchlistDao,
    private val historyDao: HistoryDao
) : MovieRepository {

    private val cache = mutableMapOf<String, Movie>()

    override suspend fun searchMovies(query: String, page: Int): Resource<List<SearchResult>> {
        return try {
            val response = api.searchMovies(
                query = query,
                page = page,
                type = null
            )
            if (response.response == "False") {
                Resource.Error(response.error ?: "No results")
            } else {
                val results = response.search?.map { it.toDomain() } ?: emptyList()
                Resource.Success(results)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getMovieById(imdbId: String): Resource<Movie> {
        cache[imdbId]?.let { return Resource.Success(it) }
        return try {
            val dto = api.getMovieById(
                imdbId = imdbId,
                plot = "full"
            )
            if (dto.response == "False") {
                Resource.Error(dto.error ?: "Unknown error")
            } else {
                val movie = dto.toDomain()
                cache[imdbId] = movie
                Resource.Success(movie)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getMoviesByIds(ids: List<String>): List<Movie> =
        ids.mapNotNull { id ->
            (getMovieById(id) as? Resource.Success)?.data
        }

    override fun getWatchlist(): Flow<List<WatchlistEntity>> = watchlistDao.getAll()

    override suspend fun addToWatchlist(movie: Movie) =
        watchlistDao.insert(movie.toWatchlistEntity())

    override suspend fun removeFromWatchlist(imdbId: String) =
        watchlistDao.deleteById(imdbId)

    override suspend fun isInWatchlist(imdbId: String): Boolean =
        watchlistDao.exists(imdbId)

    private fun Movie.toWatchlistEntity() = WatchlistEntity(
        imdbId = imdbId,
        title = title,
        year = year,
        poster = poster,
        genre   = genres.joinToString(", "),
        rating = imdbRating
    )
}