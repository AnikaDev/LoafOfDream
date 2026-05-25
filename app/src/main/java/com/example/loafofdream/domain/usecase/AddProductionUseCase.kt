package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.repository.IProductionRepository

class AddProductionUseCase(private val repository: IProductionRepository) {
    suspend operator fun invoke(productId: Int, quantity: Int, date: String) =
        repository.addProduction(productId, quantity, date)
}
