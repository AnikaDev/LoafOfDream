package com.example.loafofdream.domain.model

data class AuthResult(
    val token: String,
    val role: String,
    val name: String,
    val userId: Int
)
