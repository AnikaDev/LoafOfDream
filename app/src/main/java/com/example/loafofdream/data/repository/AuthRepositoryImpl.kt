package com.example.loafofdream.data.repository

import com.example.loafofdream.data.api.RetrofitClient
import com.example.loafofdream.data.models.LoginRequest
import com.example.loafofdream.data.models.RegisterRequest
import com.example.loafofdream.domain.model.AuthResult
import com.example.loafofdream.domain.repository.IAuthRepository

class AuthRepositoryImpl : IAuthRepository {
    private val api = RetrofitClient.api

    override suspend fun login(email: String, password: String): AuthResult {
        val response = api.login(LoginRequest(email, password))
        if (!response.isSuccessful) throw Exception("Неверный email или пароль")
        val body = response.body()!!
        return AuthResult(body.token, body.role, body.name, body.userId)
    }

    override suspend fun register(name: String, email: String, password: String, role: String) {
        val response = api.register(RegisterRequest(name, email, password, role))
        if (response.code() == 409) throw Exception("Email уже используется")
        if (!response.isSuccessful) throw Exception("Ошибка регистрации")
    }
}
