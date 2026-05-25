package com.example.loafofdream.domain.usecase

import com.example.loafofdream.domain.model.SaleItem
import com.example.loafofdream.domain.repository.ISalesRepository

class AddSalesUseCase(private val repository: ISalesRepository) {
    suspend operator fun invoke(items: List<SaleItem>, date: String) =
        repository.addSales(items, date)
}
