package com.neonnoir.Data.local.dao

import androidx.room.*
import com.neonnoir.Data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    // Returns a live stream of all watchlist items ordered by most recently added
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    // Inserts or replaces a watchlist entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    // Removes the watchlist entry with the given IMDB ID
    @Query("DELETE FROM watchlist WHERE imdbId = :imdbId")
    suspend fun deleteById(imdbId: String)

    // Returns true if an entry with the given IMDB ID already exists
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbId = :imdbId)")
    suspend fun exists(imdbId: String): Boolean

    // Returns the total count of saved watchlist items
    @Query("SELECT COUNT(*) FROM watchlist")
    fun getCount(): Flow<Int>
}