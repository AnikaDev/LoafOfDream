package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.repository.IProductRepository

class DeleteProductUseCase(private val repository: IProductRepository) {
    suspend operator fun invoke(id: Int) = repository.deleteProduct(id)
}
