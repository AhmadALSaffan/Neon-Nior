package com.neonnoir.Data.local.dao

import androidx.room.*
import com.neonnoir.Data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    // Returns all watch history ordered by most recently watched
    @Query("SELECT * FROM history ORDER BY watchedAt DESC")
    fun getAll(): Flow<List<HistoryEntity>>

    // Inserts or replaces a history entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    // Removes all history entries
    @Query("DELETE FROM history")
    suspend fun clearAll()
}