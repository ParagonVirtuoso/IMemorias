package com.github.ParagonVirtuoso.memorias.domain.repository

import com.github.ParagonVirtuoso.memorias.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    fun getCurrentUser(): Flow<User?>
    fun isUserAuthenticated(): Flow<Boolean>
} 