package com.example.loafofdream.data.repository

import com.example.loafofdream.data.api.RetrofitClient
import com.example.loafofdream.data.models.CreateProductRequest
import com.example.loafofdream.data.models.ProductDto
import com.example.loafofdream.domain.model.Product
import com.example.loafofdream.domain.model.ProductRequest
import com.example.loafofdream.domain.repository.IProductRepository

class ProductRepositoryImpl : IProductRepository {
    private val api = RetrofitClient.api

    override suspend fun getProducts(query: String?, category: String?, date: String?): List<Product> {
        val response = api.getProducts(query, category, date)
        if (!response.isSuccessful) throw Exception("Ошибка загрузки данных")
        return response.body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getProduct(id: Int): Product {
        val response = api.getProduct(id)
        if (!response.isSuccessful) throw Exception("Ошибка загрузки продукта")
        return response.body()!!.toDomain()
    }

    override suspend fun createProduct(request: ProductRequest): Int {
        val response = api.createProduct(request.toDto())
        if (!response.isSuccessful) throw Exception("Ошибка создания продукта")
        return response.body()!!.id
    }

    override suspend fun updateProduct(id: Int, request: ProductRequest) {
        val response = api.updateProduct(id, request.toDto())
        if (!response.isSuccessful) throw Exception("Ошибка обновления продукта")
    }

    override suspend fun deleteProduct(id: Int) {
        val response = api.deleteProduct(id)
        if (!response.isSuccessful) throw Exception("Ошибка удаления")
    }

    private fun ProductDto.toDomain() =
        Product(id, name, category, price, quantity, shelfLifeHours, photoUrl, lastProducedAt)

    private fun ProductRequest.toDto() =
        CreateProductRequest(name, category, price, quantity, shelfLifeHours, photoUrl)
}
