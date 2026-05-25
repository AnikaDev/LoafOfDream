package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.Product
import com.example.loafofdream.domain.repository.IProductRepository

class GetProductUseCase(private val repository: IProductRepository) {
    suspend operator fun invoke(id: Int): Product = repository.getProduct(id)
}
