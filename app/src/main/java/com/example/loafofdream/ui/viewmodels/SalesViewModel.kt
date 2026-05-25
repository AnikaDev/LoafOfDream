package com.example.loafofdream.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loafofdream.data.repository.SalesRepositoryImpl
import com.example.loafofdream.domain.model.SaleItem
import com.example.loafofdream.domain.model.SaleRecord
import com.example.loafofdream.domain.usecase.AddSalesUseCase
import com.example.loafofdream.domain.usecase.GetSalesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class SalesViewModel : ViewModel() {

    private val repository = SalesRepositoryImpl()
    private val addSalesUseCase = AddSalesUseCase(repository)
    private val getSalesUseCase = GetSalesUseCase(repository)

    private val _records = MutableStateFlow<List<SaleRecord>>(emptyList())
    val records: StateFlow<List<SaleRecord>> = _records

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result

    fun loadSales(date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _records.value = getSalesUseCase(date)
            } catch (e: Exception) {
                _result.value = e.message ?: "Ошибка загрузки продаж"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSales(items: List<SaleItem>, date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                addSalesUseCase(items, date)
                _result.value = "Продажи зафиксированы"
                loadSales(date)
            } catch (e: Exception) {
                _result.value = e.message ?: "Ошибка: проверьте наличие товара"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() { _result.value = null }
}
