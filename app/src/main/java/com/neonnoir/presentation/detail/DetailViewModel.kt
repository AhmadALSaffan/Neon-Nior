package com.neonnoir.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neonnoir.Data.repository.MovieRepository
import com.neonnoir.domain.model.Movie
import com.neonnoir.domain.model.SearchResult
import com.neonnoir.domain.usecase.AddToWatchlistUseCase
import com.neonnoir.domain.usecase.GetMovieByIdUseCase
import com.neonnoir.domain.usecase.RemoveFromWatchlistUseCase
import com.neonnoir.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getMovieByIdUseCase: GetMovieByIdUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<DetailUiState>(DetailUiState.Loading)
    val uiState: LiveData<DetailUiState> = _uiState

    private val _isInWatchlist = MutableLiveData<Boolean>(false)
    val isInWatchlist: LiveData<Boolean> = _isInWatchlist

    private val _relatedMovies = MutableLiveData<List<SearchResult>>()
    val relatedMovies: LiveData<List<SearchResult>> = _relatedMovies

    // Loads the movie details, watchlist status, and related movies in parallel
    fun load(imdbId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading

            val movieResult       = getMovieByIdUseCase(imdbId)
            val inWatchlistResult = async { repository.isInWatchlist(imdbId) }

            _isInWatchlist.value = inWatchlistResult.await()

            when (movieResult) {
                is Resource.Success -> {
                    _uiState.value = DetailUiState.Success(movieResult.data)
                    loadRelated(movieResult.data, imdbId)
                }
                is Resource.Error -> {
                    _uiState.value = DetailUiState.Error(movieResult.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // Searches for movies in the same primary genre and excludes the current one
    private suspend fun loadRelated(movie: Movie, currentId: String) {
        val genre   = movie.genres.firstOrNull() ?: return
        val result  = repository.searchMovies(genre)
        if (result is Resource.Success) {
            _relatedMovies.value = result.data.filter { it.imdbId != currentId }
        }
    }

    // Adds or removes the movie from the watchlist and flips the toggle state
    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            val currently = _isInWatchlist.value ?: false
            if (currently) {
                removeFromWatchlistUseCase(movie.imdbId)
            } else {
                addToWatchlistUseCase(movie)
            }
            _isInWatchlist.value = !currently
        }
    }
}

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val movie: Movie) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}
