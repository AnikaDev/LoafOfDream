package com.example.loafofdream.domain.repository

import com.example.loafofdream.domain.model.Product
import com.example.loafofdream.domain.model.ProductRequest

interface IProductRepository {
    suspend fun getProducts(query: String?, category: String?, date: String? = null): List<Product>
    suspend fun getProduct(id: Int): Product
    suspend fun createProduct(request: ProductRequest): Int
    suspend fun updateProduct(id: Int, request: ProductRequest)
    suspend fun deleteProduct(id: Int)
}
