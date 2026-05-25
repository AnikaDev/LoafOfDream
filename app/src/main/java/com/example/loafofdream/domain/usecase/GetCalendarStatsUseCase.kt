package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.CalendarStats
import com.example.loafofdream.domain.repository.IStatsRepository

class GetCalendarStatsUseCase(private val repository: IStatsRepository) {
    suspend operator fun invoke(date: String): CalendarStats =
        repository.getCalendarStats(date)
}
