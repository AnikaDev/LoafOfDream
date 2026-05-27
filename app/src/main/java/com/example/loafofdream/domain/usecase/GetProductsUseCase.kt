package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.Product
import com.example.loafofdream.domain.repository.IProductRepository

class GetProductsUseCase(private val repository: IProductRepository) {
    suspend operator fun invoke(query: String? = null, category: String? = null, date: String? = null): List<Product> =
        repository.getProducts(query, category, date)
}
