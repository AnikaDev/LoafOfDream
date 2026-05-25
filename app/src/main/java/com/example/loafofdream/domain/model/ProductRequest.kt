package com.example.loafofdream.domain.model

data class ProductRequest(
    val name: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val shelfLifeHours: Int,
    val photoUrl: String?
)
