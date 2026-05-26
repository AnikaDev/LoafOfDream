package com.example.loafofdream.domain.repository

import com.example.loafofdream.domain.model.SaleItem
import com.example.loafofdream.domain.model.SaleRecord

interface ISalesRepository {
    suspend fun addSales(items: List<SaleItem>, date: String)
    suspend fun getSales(date: String): List<SaleRecord>
    suspend fun deleteSale(id: Int)
}
