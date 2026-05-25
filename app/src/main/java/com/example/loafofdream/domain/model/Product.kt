package com.example.loafofdream.domain.model

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val shelfLifeHours: Int,
    val photoUrl: String?,
    val lastProducedAt: String?
)
