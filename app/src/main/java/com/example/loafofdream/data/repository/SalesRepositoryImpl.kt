package com.example.loafofdream.data.repository

import com.example.loafofdream.data.api.RetrofitClient
import com.example.loafofdream.data.models.SaleRequest
import com.example.loafofdream.domain.model.SaleItem
import com.example.loafofdream.domain.model.SaleRecord
import com.example.loafofdream.domain.repository.ISalesRepository
import com.example.loafofdream.data.models.SaleItem as SaleItemDto

class SalesRepositoryImpl : ISalesRepository {
    private val api = RetrofitClient.api

    override suspend fun addSales(items: List<SaleItem>, date: String) {
        val dtoItems = items.map { SaleItemDto(it.productId, it.quantity) }
        val response = api.addSales(SaleRequest(dtoItems, date))
        if (!response.isSuccessful) throw Exception("Ошибка: проверьте наличие товара")
    }

    override suspend fun getSales(date: String): List<SaleRecord> {
        val response = api.getSales(date)
        if (!response.isSuccessful) throw Exception("Ошибка загрузки продаж")
        return response.body()?.map {
            SaleRecord(it.id, it.productId, it.productName, it.quantity, it.priceAtTime, it.date)
        } ?: emptyList()
    }
}
