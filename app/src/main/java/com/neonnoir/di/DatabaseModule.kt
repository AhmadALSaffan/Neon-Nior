package com.neonnoir.di

import android.content.Context
import androidx.room.Room
import com.neonnoir.Data.local.db.AppDatabase
import com.neonnoir.Data.local.dao.HistoryDao
import com.neonnoir.Data.local.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext  // ← ONLY this one
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Builds and provides the singleton Room database
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "neon_noir.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    // Provides WatchlistDao from the database instance
    @Provides
    @Singleton
    fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()

    // Provides HistoryDao from the database instance
    @Provides
    @Singleton
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
}