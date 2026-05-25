package com.example.loafofdream.domain.repository

import com.example.loafofdream.domain.model.AuthResult

interface IAuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(name: String, email: String, password: String, role: String)
}
