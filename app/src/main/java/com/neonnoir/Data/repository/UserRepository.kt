package com.neonnoir.Data.repository

import com.neonnoir.Data.model.UserModel

interface UserRepository {
    suspend fun saveUser(user: UserModel): Result<Unit>
    suspend fun getUser(uid: String): Result<UserModel>
    suspend fun updateLastLogin(uid: String): Result<Unit>
    suspend fun deleteUser(uid: String): Result<Unit>
}