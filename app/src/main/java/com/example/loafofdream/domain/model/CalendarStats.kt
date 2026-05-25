package com.example.loafofdream.domain.model

data class CalendarStats(
    val date: String,
    val sales: List<SaleRecord>,
    val dayRevenue: Double,
    val dayCount: Int,
    val monthRevenue: Double,
    val monthCount: Int,
    val yearRevenue: Double,
    val yearCount: Int
)
