package com.neonnoir.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.neonnoir.Data.local.entity.HistoryEntity
import com.neonnoir.Data.local.entity.WatchlistEntity
import com.neonnoir.domain.usecase.GetHistoryUseCase
import com.neonnoir.domain.usecase.GetWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    getWatchlistUseCase: GetWatchlistUseCase,
    getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    // Live stream of watchlist items from Room, ordered by most recently added
    val watchlist: LiveData<List<WatchlistEntity>> = getWatchlistUseCase().asLiveData()

    // Live stream of watch history from Room, ordered by most recently watched
    val history: LiveData<List<HistoryEntity>> = getHistoryUseCase().asLiveData()
}
