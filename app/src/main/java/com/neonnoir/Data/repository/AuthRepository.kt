package com.neonnoir.Data.repository

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<Unit>
    suspend fun signOut()
    fun isLoggedIn(): Boolean
}