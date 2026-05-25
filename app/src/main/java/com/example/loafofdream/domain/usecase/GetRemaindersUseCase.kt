package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.Remainder
import com.example.loafofdream.domain.repository.IStatsRepository

class GetRemaindersUseCase(private val repository: IStatsRepository) {
    suspend operator fun invoke(): List<Remainder> = repository.getRemainders()
}
