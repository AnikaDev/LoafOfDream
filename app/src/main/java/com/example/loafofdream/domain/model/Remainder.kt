package com.example.loafofdream.domain.model

data class Remainder(
    val productId: Int,
    val productName: String,
    val category: String,
    val quantity: Int,
    val price: Double,
    val isExpired: Boolean,
    val photoUrl: String?
)
