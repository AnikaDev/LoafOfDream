package com.example.loafofdream.domain.repository

import com.example.loafofdream.domain.model.CalendarStats
import com.example.loafofdream.domain.model.Remainder
import com.example.loafofdream.domain.model.Revenue

interface IStatsRepository {
    suspend fun getRemainders(): List<Remainder>
    suspend fun getRevenue(period: String, date: String?): Revenue
    suspend fun getCalendarStats(date: String): CalendarStats
}
