package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String, val role: String)

@Serializable
data class AuthResponse(val token: String, val role: String, val name: String, val userId: Int)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class IdResponse(val id: Int)

@Serializable
data class ProductDto(
    val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val shelfLifeHours: Int,
    val photoUrl: String? = null,
    val lastProducedAt: String? = null
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val category: String,
    val price: Double,
    val quantity: Int = 0,
    val shelfLifeHours: Int,
    val photoUrl: String? = null
)

@Serializable
data class ProductionRequest(
    val productId: Int,
    val quantity: Int,
    val date: String
)

@Serializable
data class ProductionRecordDto(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val date: String
)

@Serializable
data class SaleItem(val productId: Int, val quantity: Int)

@Serializable
data class SaleRequest(val items: List<SaleItem>, val date: String)

@Serializable
data class SaleRecordDto(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val priceAtTime: Double,
    val date: String
)

@Serializable
data class RemainderDto(
    val productId: Int,
    val productName: String,
    val category: String,
    val quantity: Int,
    val price: Double,
    val isExpired: Boolean,
    val photoUrl: String? = null
)

@Serializable
data class RevenueResponse(
    val period: String,
    val total: Double,
    val salesCount: Int
)

@Serializable
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
