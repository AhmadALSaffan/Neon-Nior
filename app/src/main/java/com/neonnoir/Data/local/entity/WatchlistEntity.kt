package com.neonnoir.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val imdbId:  String,
    val title:   String,
    val year:    String,
    val poster:  String,
    val genre:   String,
    val rating:  Float,
    val addedAt: Long = System.currentTimeMillis()
)