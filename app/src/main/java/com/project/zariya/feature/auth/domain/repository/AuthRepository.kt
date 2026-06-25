package com.project.zariya.feature.auth.domain.repository

import com.project.zariya.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: AuthUser?
    val isLoggedIn: Boolean
    fun observeAuthState(): Flow<AuthUser?>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signOut()
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}
