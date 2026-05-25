package com.example.loafofdream.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(val name: String, val email: String, val password: String, val role: String)

data class AuthResponse(
    val token: String,
    val role: String,
    val name: String,
    val userId: Int
)

data class MessageResponse(val message: String)

data class IdResponse(val id: Int)

data class ProductDto(
    val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    @SerializedName("shelfLifeHours") val shelfLifeHours: Int,
    val photoUrl: String? = null,
    val lastProducedAt: String? = null
)

data class CreateProductRequest(
    val name: String,
    val category: String,
    val price: Double,
    val quantity: Int = 0,
    val shelfLifeHours: Int,
    val photoUrl: String? = null
)

data class ProductionRequest(val productId: Int, val quantity: Int, val date: String)

data class ProductionRecordDto(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val date: String
)

data class SaleItem(val productId: Int, val quantity: Int)

data class SaleRequest(val items: List<SaleItem>, val date: String)

data class SaleRecordDto(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val priceAtTime: Double,
    val date: String
)

data class RemainderDto(
    val productId: Int,
    val productName: String,
    val category: String,
    val quantity: Int,
    val price: Double,
    val isExpired: Boolean,
    val photoUrl: String? = null
)

data class RevenueResponse(
    val period: String,
    val total: Double,
    val salesCount: Int
)

data class CalendarDayStats(
    val date: String,
    val sales: List<SaleRecordDto>,
    val dayRevenue: Double,
    val dayCount: Int,
    val monthRevenue: Double,
    val monthCount: Int,
    val yearRevenue: Double,
    val yearCount: Int
)
