package com.example.loafofdream.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loafofdream.data.repository.ProductionRepositoryImpl
import com.example.loafofdream.domain.model.ProductionRecord
import com.example.loafofdream.domain.usecase.AddProductionUseCase
import com.example.loafofdream.domain.usecase.GetProductionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ProductionViewModel : ViewModel() {

    private val repository = ProductionRepositoryImpl()
    private val addProductionUseCase = AddProductionUseCase(repository)
    private val getProductionUseCase = GetProductionUseCase(repository)

    private val _records = MutableStateFlow<List<ProductionRecord>>(emptyList())
    val records: StateFlow<List<ProductionRecord>> = _records

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result

    fun loadRecords(date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _records.value = getProductionUseCase(date)
            } catch (e: Exception) {
                _result.value = e.message ?: "Ошибка загрузки записей"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addProduction(productId: Int, quantity: Int, date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                addProductionUseCase(productId, quantity, date)
                _result.value = "Производство зафиксировано"
                loadRecords(date)
            } catch (e: Exception) {
                _result.value = e.message ?: "Ошибка подключения к серверу"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() { _result.value = null }
}
