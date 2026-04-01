package com.neonnoir.Data.repository

import com.google.firebase.database.FirebaseDatabase
import com.neonnoir.Data.model.UserModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {

    // Reference to the "users" node in Firebase Realtime Database
    private val db = FirebaseDatabase.getInstance().getReference("users")

    // Saves full user object under users/{uid}
    override suspend fun saveUser(user: UserModel): Result<Unit> = try {
        db.child(user.uid).setValue(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Reads user data from users/{uid} once
    override suspend fun getUser(uid: String): Result<UserModel> = try {
        val snapshot = db.child(uid).get().await()
        val user = snapshot.getValue(UserModel::class.java)
        if (user != null) Result.success(user)
        else Result.failure(Exception("User not found"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Updates only the lastLoginAt timestamp under users/{uid}
    override suspend fun updateLastLogin(uid: String): Result<Unit> = try {
        db.child(uid).child("lastLoginAt")
            .setValue(System.currentTimeMillis()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Removes the user node from Firebase Realtime Database
    override suspend fun deleteUser(uid: String): Result<Unit> = try {
        db.child(uid).removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}