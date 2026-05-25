package com.example.loafofdream.domain.repository

import com.example.loafofdream.domain.model.ProductionRecord

interface IProductionRepository {
    suspend fun addProduction(productId: Int, quantity: Int, date: String)
    suspend fun getProduction(date: String): List<ProductionRecord>
}
