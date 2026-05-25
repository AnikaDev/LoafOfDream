package com.example.loafofdream.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loafofdream.data.local.PreferencesManager
import com.example.loafofdream.data.repository.AuthRepositoryImpl
import com.example.loafofdream.domain.usecase.LoginUseCase
import com.example.loafofdream.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val prefs: PreferencesManager) : ViewModel() {

    private val repository = AuthRepositoryImpl()
    private val loginUseCase = LoginUseCase(repository)
    private val registerUseCase = RegisterUseCase(repository)

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val result = loginUseCase(email, password)
                prefs.token = result.token
                prefs.userRole = result.role
                prefs.userName = result.name
                prefs.userId = result.userId
                _state.value = AuthState.Success
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Ошибка подключения к серверу")
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                registerUseCase(name, email, password, role)
                _state.value = AuthState.Success
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Ошибка подключения к серверу")
            }
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }
}
