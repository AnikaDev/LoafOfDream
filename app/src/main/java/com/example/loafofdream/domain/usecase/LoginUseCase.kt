package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.AuthResult
import com.example.loafofdream.domain.repository.IAuthRepository

class LoginUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(name: String, password: String): AuthResult =
        repository.login(name, password)
}
