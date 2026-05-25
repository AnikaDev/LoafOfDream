package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.repository.IAuthRepository

class RegisterUseCase(private val repository: IAuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String, role: String) =
        repository.register(name, email, password, role)
}
