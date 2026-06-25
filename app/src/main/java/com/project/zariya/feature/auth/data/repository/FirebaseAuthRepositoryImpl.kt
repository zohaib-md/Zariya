package com.project.zariya.feature.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.project.zariya.feature.auth.domain.model.AuthUser
import com.project.zariya.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.project.zariya.core.data.database.ZariyaDatabase

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: ZariyaDatabase
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.let { user ->
            AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString(),
                isEmailVerified = user.isEmailVerified
            )
        }

    override val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    override fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.let { user ->
                AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    isEmailVerified = user.isEmailVerified
                )
            })
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign in failed"))
            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    isEmailVerified = user.isEmailVerified
                )
            )
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign up failed"))

            // Update display name
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            user.updateProfile(profileUpdates).await()

            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = displayName,
                    photoUrl = user.photoUrl?.toString(),
                    isEmailVerified = user.isEmailVerified
                )
            )
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("Google sign in failed"))
            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    isEmailVerified = user.isEmailVerified
                )
            )
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    private fun mapFirebaseException(e: Exception): Exception {
        val message = when {
            e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                "Invalid email or password"
            e.message?.contains("EMAIL_EXISTS") == true || e.message?.contains("already in use") == true ->
                "An account with this email already exists"
            e.message?.contains("WEAK_PASSWORD") == true ->
                "Password must be at least 6 characters"
            e.message?.contains("INVALID_EMAIL") == true || e.message?.contains("badly formatted") == true ->
                "Please enter a valid email address"
            e.message?.contains("NETWORK") == true ->
                "Network error. Please check your connection"
            e.message?.contains("TOO_MANY_ATTEMPTS") == true ->
                "Too many attempts. Please try again later"
            else -> e.localizedMessage ?: "An unexpected error occurred"
        }
        return Exception(message)
    }
}
