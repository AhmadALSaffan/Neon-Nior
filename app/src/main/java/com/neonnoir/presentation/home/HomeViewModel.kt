package com.neonnoir.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neonnoir.constants.Editorial
import com.neonnoir.domain.model.Movie
import com.neonnoir.domain.usecase.GetMovieByIdUseCase
import com.neonnoir.domain.usecase.GetTrendingUseCase
import com.neonnoir.presentation.common.adapters.GenreItem
import com.neonnoir.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMovieByIdUseCase: GetMovieByIdUseCase,
    private val getTrendingUseCase: GetTrendingUseCase
) : ViewModel() {

    private val _heroMovie = MutableLiveData<Resource<Movie>>()
    val heroMovie: LiveData<Resource<Movie>> = _heroMovie

    private val _trendingMovies = MutableLiveData<Resource<List<Movie>>>()
    val trendingMovies: LiveData<Resource<List<Movie>>> = _trendingMovies

    private val _recentMovies = MutableLiveData<Resource<List<Movie>>>()
    val recentMovies: LiveData<Resource<List<Movie>>> = _recentMovies

    private val _genreItems = MutableLiveData<List<GenreItem>>()
    val genreItems: LiveData<List<GenreItem>> = _genreItems

    // Triggers all three data loads in parallel on first creation
    init { loadAll() }

    // Launches hero, trending, and recently-added fetches concurrently
    private fun loadAll() {
        viewModelScope.launch {
            _heroMovie.value = Resource.Loading
            _trendingMovies.value = Resource.Loading
            _recentMovies.value = Resource.Loading

            val heroDeferred     = async { getMovieByIdUseCase(Editorial.FEATURED_HERO_ID) }
            val trendingDeferred = async { getTrendingUseCase(Editorial.TRENDING_IDS) }
            val recentDeferred   = async { getTrendingUseCase(Editorial.RECENTLY_ADDED_IDS) }

            _heroMovie.value     = heroDeferred.await()
            val trendingList     = trendingDeferred.await()
            val recentList       = recentDeferred.await()

            _trendingMovies.value = Resource.Success(trendingList)
            _recentMovies.value   = Resource.Success(recentList)

            buildGenreItems(trendingList)
        }
    }

    // Builds genre tile items using curated labels and trending movie posters as covers
    private fun buildGenreItems(movies: List<Movie>) {
        val genreEntries = Editorial.GENRE_SEARCHES.entries.toList()
        val items = genreEntries.mapIndexed { index, (label, keyword) ->
            GenreItem(
                label      = label,
                keyword    = keyword,
                coverUrl   = movies.getOrNull(index)?.poster ?: ""
            )
        }
        _genreItems.value = items
    }

    // Re-triggers all data loads (useful for pull-to-refresh)
    fun refresh() { loadAll() }
}