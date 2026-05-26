package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.repository.IProductionRepository

class DeleteProductionUseCase(private val repository: IProductionRepository) {
    suspend operator fun invoke(id: Int) = repository.deleteProduction(id)
}
