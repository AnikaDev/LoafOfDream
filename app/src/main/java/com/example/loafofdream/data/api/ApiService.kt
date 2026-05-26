package com.example.loafofdream.data.api

import com.example.loafofdream.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("products")
    suspend fun getProducts(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null
    ): Response<List<ProductDto>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<ProductDto>

    @POST("products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<IdResponse>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body request: CreateProductRequest): Response<MessageResponse>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<MessageResponse>

    @POST("production")
    suspend fun addProduction(@Body request: ProductionRequest): Response<MessageResponse>

    @DELETE("production/{id}")
    suspend fun deleteProduction(@Path("id") id: Int): Response<MessageResponse>

    @GET("production")
    suspend fun getProduction(@Query("date") date: String): Response<List<ProductionRecordDto>>

    @POST("sales")
    suspend fun addSales(@Body request: SaleRequest): Response<MessageResponse>

    @GET("sales")
    suspend fun getSales(@Query("date") date: String): Response<List<SaleRecordDto>>

    @GET("remainders")
    suspend fun getRemainders(): Response<List<RemainderDto>>

    @GET("stats/revenue")
    suspend fun getRevenue(
        @Query("period") period: String,
        @Query("date") date: String? = null
    ): Response<RevenueResponse>

    @GET("stats/calendar")
    suspend fun getCalendarStats(@Query("date") date: String): Response<CalendarDayStats>
}
