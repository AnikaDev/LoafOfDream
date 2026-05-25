package com.example.loafofdream.data.repository

import com.example.loafofdream.data.api.RetrofitClient
import com.example.loafofdream.domain.model.CalendarStats
import com.example.loafofdream.domain.model.Remainder
import com.example.loafofdream.domain.model.Revenue
import com.example.loafofdream.domain.model.SaleRecord
import com.example.loafofdream.domain.repository.IStatsRepository

class StatsRepositoryImpl : IStatsRepository {
    private val api = RetrofitClient.api

    override suspend fun getRemainders(): List<Remainder> {
        val response = api.getRemainders()
        if (!response.isSuccessful) throw Exception("Ошибка загрузки остатков")
        return response.body()?.map {
            Remainder(it.productId, it.productName, it.category, it.quantity, it.price, it.isExpired, it.photoUrl)
        } ?: emptyList()
    }

    override suspend fun getRevenue(period: String, date: String?): Revenue {
        val response = api.getRevenue(period, date)
        if (!response.isSuccessful) throw Exception("Ошибка загрузки выручки")
        val body = response.body()!!
        return Revenue(body.period, body.total, body.salesCount)
    }

    override suspend fun getCalendarStats(date: String): CalendarStats {
        val response = api.getCalendarStats(date)
        if (!response.isSuccessful) throw Exception("Ошибка загрузки данных")
        val body = response.body()!!
        val sales = body.sales.map {
            SaleRecord(it.id, it.productId, it.productName, it.quantity, it.priceAtTime, it.date)
        }
        return CalendarStats(body.date, sales, body.dayRevenue, body.dayCount,
            body.monthRevenue, body.monthCount, body.yearRevenue, body.yearCount)
    }
}
