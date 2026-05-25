package com.example.loafofdream.domain.model

data class ProductionRecord(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val date: String
)
