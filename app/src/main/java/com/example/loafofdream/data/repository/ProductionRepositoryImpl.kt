package com.example.loafofdream.data.repository

import com.example.loafofdream.data.api.RetrofitClient
import com.example.loafofdream.data.models.ProductionRequest
import com.example.loafofdream.domain.model.ProductionRecord
import com.example.loafofdream.domain.repository.IProductionRepository

class ProductionRepositoryImpl : IProductionRepository {
    private val api = RetrofitClient.api

    override suspend fun addProduction(productId: Int, quantity: Int, date: String) {
        val response = api.addProduction(ProductionRequest(productId, quantity, date))
        if (!response.isSuccessful) throw Exception("Ошибка записи производства")
    }

    override suspend fun getProduction(date: String): List<ProductionRecord> {
        val response = api.getProduction(date)
        if (!response.isSuccessful) throw Exception("Ошибка загрузки записей")
        return response.body()?.map {
            ProductionRecord(it.id, it.productId, it.productName, it.quantity, it.date)
        } ?: emptyList()
    }
}
