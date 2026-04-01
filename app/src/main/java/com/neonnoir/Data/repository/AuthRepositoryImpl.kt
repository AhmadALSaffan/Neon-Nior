package com.neonnoir.Data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.neonnoir.Data.model.UserModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthRepository {

    // Creates Firebase account → sets display name → saves to Realtime DB
    override suspend fun signUpWithEmail(
        name: String,
        email: String,
        password: String
    ): Result<Unit> = try {
        // 1. Create Firebase Auth account
        val result = firebaseAuth
            .createUserWithEmailAndPassword(email, password).await()

        result.user?.let { fbUser ->
            // 2. Set display name on Firebase Auth profile
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            fbUser.updateProfile(profileUpdate).await()

            // 3. Save user data to Firebase Realtime Database
            userRepository.saveUser(
                UserModel(
                    uid         = fbUser.uid,
                    displayName = name,
                    email       = email,
                    photoUrl    = "",
                    createdAt   = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
            )
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Signs in → updates lastLoginAt in Realtime DB
    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<Unit> = try {
        val result = firebaseAuth
            .signInWithEmailAndPassword(email, password).await()

        result.user?.let { fbUser ->
            userRepository.updateLastLogin(fbUser.uid)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Signs out from Firebase Auth
    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null
}