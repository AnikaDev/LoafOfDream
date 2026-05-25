package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.ProductionRecord
import com.example.loafofdream.domain.repository.IProductionRepository

class GetProductionUseCase(private val repository: IProductionRepository) {
    suspend operator fun invoke(date: String): List<ProductionRecord> =
        repository.getProduction(date)
}
