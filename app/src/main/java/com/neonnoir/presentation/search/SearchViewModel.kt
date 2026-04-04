package com.neonnoir.presentation.search

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neonnoir.constants.Editorial
import com.neonnoir.domain.model.SearchResult
import com.neonnoir.domain.usecase.GetTrendingUseCase
import com.neonnoir.domain.usecase.SearchMoviesUseCase
import com.neonnoir.presentation.common.adapters.GenreItem
import com.neonnoir.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.searchDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "search_prefs")

private val RECENT_SEARCHES_KEY = stringSetPreferencesKey("recent_searches")

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase,
    private val getTrendingUseCase: GetTrendingUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private val _results = MutableLiveData<Resource<List<SearchResult>>>()
    val results: LiveData<Resource<List<SearchResult>>> = _results

    private val _popularMovies = MutableLiveData<Resource<List<SearchResult>>>()
    val popularMovies: LiveData<Resource<List<SearchResult>>> = _popularMovies

    private val _genreItems = MutableLiveData<List<GenreItem>>()
    val genreItems: LiveData<List<GenreItem>> = _genreItems

    private val _recentSearches = MutableLiveData<List<String>>(emptyList())
    val recentSearches: LiveData<List<String>> = _recentSearches

    init {
        observeQuery()
        loadPopular()
        loadGenreCovers()
        collectRecentSearches()
    }

    // Debounces query input: waits 400ms after last keystroke, filters blanks, dedupes
    private fun observeQuery() {
        viewModelScope.launch {
            _query
                .debounce(400)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    _results.postValue(Resource.Loading)
                    _results.postValue(searchMoviesUseCase(query))
                }
        }
    }

    // Fetches "2024" results and strips out any items that have no poster
    private fun loadPopular() {
        viewModelScope.launch {
            _popularMovies.value = Resource.Loading
            val result = searchMoviesUseCase("2024")
            _popularMovies.value = when (result) {
                is Resource.Success -> Resource.Success(result.data.filter { it.poster.isNotBlank() })
                else -> result
            }
        }
    }

    // Fetches each genre's curated cover movie by ID and maps its poster to the tile.
    // Uses GENRE_COVER_IDS so every genre gets a visually correct, poster-guaranteed film.
    // Results are served from the in-memory cache after the first fetch.
    private fun loadGenreCovers() {
        viewModelScope.launch {
            val movies = getTrendingUseCase(Editorial.GENRE_COVER_IDS)
            val entries = Editorial.GENRE_SEARCHES.entries.toList()
            val items = entries.mapIndexed { index, (label, keyword) ->
                GenreItem(
                    label    = label,
                    keyword  = keyword,
                    coverUrl = movies.getOrNull(index)?.poster ?: ""
                )
            }
            _genreItems.value = items
        }
    }

    // Collects recent searches from DataStore and emits up to 10 most recent
    private fun collectRecentSearches() {
        viewModelScope.launch {
            context.searchDataStore.data
                .map { prefs ->
                    prefs[RECENT_SEARCHES_KEY]?.toList() ?: emptyList()
                }
                .collect { list ->
                    _recentSearches.postValue(list.takeLast(10).reversed())
                }
        }
    }

    // Updates the query StateFlow; debounce handles API call timing
    fun setQuery(q: String) {
        _query.value = q
    }

    // Persists a completed search query to DataStore for recent chips display
    fun saveRecentSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            context.searchDataStore.edit { prefs ->
                val current = prefs[RECENT_SEARCHES_KEY]?.toMutableSet() ?: mutableSetOf()
                current.add(query.trim())
                prefs[RECENT_SEARCHES_KEY] = current
            }
        }
    }

    // Clears all recent searches from DataStore
    fun clearRecentSearches() {
        viewModelScope.launch {
            context.searchDataStore.edit { prefs ->
                prefs.remove(RECENT_SEARCHES_KEY)
            }
        }
    }
}
