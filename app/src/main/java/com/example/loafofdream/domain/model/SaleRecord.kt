package com.example.loafofdream.domain.model

data class SaleRecord(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val priceAtTime: Double,
    val date: String
)
