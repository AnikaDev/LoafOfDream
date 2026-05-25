package com.example.loafofdream.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loafofdream.data.repository.StatsRepositoryImpl
import com.example.loafofdream.domain.model.Remainder
import com.example.loafofdream.domain.model.Revenue
import com.example.loafofdream.domain.usecase.GetRemaindersUseCase
import com.example.loafofdream.domain.usecase.GetRevenueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatsViewModel : ViewModel() {

    private val repository = StatsRepositoryImpl()
    private val getRemaindersUseCase = GetRemaindersUseCase(repository)
    private val getRevenueUseCase = GetRevenueUseCase(repository)

    private val _remainders = MutableStateFlow<List<Remainder>>(emptyList())
    val remainders: StateFlow<List<Remainder>> = _remainders

    private val _revenue = MutableStateFlow<Revenue?>(null)
    val revenue: StateFlow<Revenue?> = _revenue

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadRemainders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _remainders.value = getRemaindersUseCase()
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки остатков"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRevenue(period: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _revenue.value = getRevenueUseCase(period)
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки выручки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}
