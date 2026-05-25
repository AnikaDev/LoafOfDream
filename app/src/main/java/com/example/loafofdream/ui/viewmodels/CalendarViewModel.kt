package com.example.loafofdream.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loafofdream.data.repository.StatsRepositoryImpl
import com.example.loafofdream.domain.model.CalendarStats
import com.example.loafofdream.domain.usecase.GetCalendarStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class CalendarState {
    object Idle : CalendarState()
    object Loading : CalendarState()
    data class Success(val stats: CalendarStats) : CalendarState()
    data class Error(val message: String) : CalendarState()
}

class CalendarViewModel : ViewModel() {

    private val repository = StatsRepositoryImpl()
    private val getCalendarStatsUseCase = GetCalendarStatsUseCase(repository)

    private val _state = MutableStateFlow<CalendarState>(CalendarState.Idle)
    val state: StateFlow<CalendarState> = _state

    init {
        loadStats(LocalDate.now())
    }

    fun loadStats(date: LocalDate) {
        viewModelScope.launch {
            _state.value = CalendarState.Loading
            try {
                val stats = getCalendarStatsUseCase(date.toString())
                _state.value = CalendarState.Success(stats)
            } catch (e: Exception) {
                _state.value = CalendarState.Error(e.message ?: "Ошибка загрузки данных")
            }
        }
    }
}
