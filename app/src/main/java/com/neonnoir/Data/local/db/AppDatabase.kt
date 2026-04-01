package com.neonnoir.Data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neonnoir.Data.local.dao.HistoryDao
import com.neonnoir.Data.local.dao.WatchlistDao
import com.neonnoir.Data.local.entity.HistoryEntity
import com.neonnoir.Data.local.entity.WatchlistEntity

@Database(
    entities = [WatchlistEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Returns the WatchlistDao for watchlist operations
    abstract fun watchlistDao(): WatchlistDao

    // Returns the HistoryDao for history operations
    abstract fun historyDao(): HistoryDao
}