package com.github.ParagonVirtuoso.memorias.domain.usecase

import com.github.ParagonVirtuoso.memorias.domain.model.User
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        return authRepository.signInWithGoogle(idToken)
    }
} 