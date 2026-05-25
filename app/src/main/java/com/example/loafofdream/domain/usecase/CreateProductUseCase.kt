package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.ProductRequest
import com.example.loafofdream.domain.repository.IProductRepository

class CreateProductUseCase(private val repository: IProductRepository) {
    suspend operator fun invoke(request: ProductRequest): Int = repository.createProduct(request)
}
