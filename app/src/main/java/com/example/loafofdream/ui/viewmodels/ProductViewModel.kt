package com.example.loafofdream.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loafofdream.data.repository.ProductRepositoryImpl
import com.example.loafofdream.domain.model.Product
import com.example.loafofdream.domain.model.ProductRequest
import com.example.loafofdream.domain.usecase.CreateProductUseCase
import com.example.loafofdream.domain.usecase.DeleteProductUseCase
import com.example.loafofdream.domain.usecase.GetProductUseCase
import com.example.loafofdream.domain.usecase.GetProductsUseCase
import com.example.loafofdream.domain.usecase.UpdateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProductListState {
    object Idle : ProductListState()
    object Loading : ProductListState()
    data class Success(val products: List<Product>) : ProductListState()
    object Empty : ProductListState()
    data class Error(val message: String) : ProductListState()
}

class ProductViewModel : ViewModel() {

    private val repository = ProductRepositoryImpl()
    private val getProductsUseCase = GetProductsUseCase(repository)
    private val getProductUseCase = GetProductUseCase(repository)
    private val createProductUseCase = CreateProductUseCase(repository)
    private val updateProductUseCase = UpdateProductUseCase(repository)
    private val deleteProductUseCase = DeleteProductUseCase(repository)

    private val _listState = MutableStateFlow<ProductListState>(ProductListState.Idle)
    val listState: StateFlow<ProductListState> = _listState

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult

    private var lastQuery: String? = null
    private var lastCategory: String? = null

    fun loadProducts(query: String? = null, category: String? = null) {
        lastQuery = query
        lastCategory = category
        viewModelScope.launch {
            _listState.value = ProductListState.Loading
            try {
                val list = getProductsUseCase(query, category)
                _listState.value = if (list.isEmpty()) ProductListState.Empty
                else ProductListState.Success(list)
            } catch (e: Exception) {
                _listState.value = ProductListState.Error(e.message ?: "Ошибка подключения к серверу")
            }
        }
    }

    fun retryLastLoad() = loadProducts(lastQuery, lastCategory)

    fun loadProduct(id: Int) {
        viewModelScope.launch {
            try {
                _selectedProduct.value = getProductUseCase(id)
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Ошибка загрузки продукта"
            }
        }
    }

    fun createProduct(request: ProductRequest) {
        viewModelScope.launch {
            try {
                createProductUseCase(request)
                _actionResult.value = "Продукт создан"
                loadProducts()
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Ошибка создания продукта"
            }
        }
    }

    fun updateProduct(id: Int, request: ProductRequest) {
        viewModelScope.launch {
            try {
                updateProductUseCase(id, request)
                _actionResult.value = "Продукт обновлён"
                loadProducts()
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Ошибка обновления продукта"
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            try {
                deleteProductUseCase(id)
                _actionResult.value = "Продукт удалён"
                loadProducts()
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Ошибка удаления"
            }
        }
    }

    fun clearActionResult() { _actionResult.value = null }
}
