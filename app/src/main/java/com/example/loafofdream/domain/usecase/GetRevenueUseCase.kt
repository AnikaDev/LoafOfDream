package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.Revenue
import com.example.loafofdream.domain.repository.IStatsRepository

class GetRevenueUseCase(private val repository: IStatsRepository) {
    suspend operator fun invoke(period: String, date: String? = null): Revenue =
        repository.getRevenue(period, date)
}
