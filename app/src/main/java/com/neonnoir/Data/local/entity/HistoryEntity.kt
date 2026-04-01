package com.neonnoir.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val poster: String,
    val watchedAt: Long = System.currentTimeMillis()
)