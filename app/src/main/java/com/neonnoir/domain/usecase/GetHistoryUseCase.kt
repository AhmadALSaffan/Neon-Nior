package com.neonnoir.domain.usecase

import com.neonnoir.Data.local.dao.HistoryDao
import com.neonnoir.Data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val historyDao: HistoryDao
) {
    operator fun invoke(): Flow<List<HistoryEntity>> = historyDao.getAll()
}
