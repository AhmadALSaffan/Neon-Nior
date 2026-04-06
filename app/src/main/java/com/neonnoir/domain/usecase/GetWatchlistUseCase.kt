package com.neonnoir.domain.usecase

import com.neonnoir.Data.local.dao.WatchlistDao
import com.neonnoir.Data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchlistUseCase @Inject constructor(
    private val watchlistDao: WatchlistDao
) {
    operator fun invoke(): Flow<List<WatchlistEntity>> = watchlistDao.getAll()
}
