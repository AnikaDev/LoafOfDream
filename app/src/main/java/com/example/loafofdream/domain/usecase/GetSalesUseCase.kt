package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.SaleRecord
import com.example.loafofdream.domain.repository.ISalesRepository

class GetSalesUseCase(private val repository: ISalesRepository) {
    suspend operator fun invoke(date: String): List<SaleRecord> =
        repository.getSales(date)
}
