package com.neonnoir.di

import com.google.firebase.auth.FirebaseAuth
import com.neonnoir.Data.repository.AuthRepository
import com.neonnoir.Data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    // Provides the singleton FirebaseAuth instance
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    // Binds AuthRepositoryImpl as the concrete implementation of AuthRepository
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}