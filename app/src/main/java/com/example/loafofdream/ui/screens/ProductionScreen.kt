package com.example.loafofdream.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.loafofdream.domain.model.Product
import com.example.loafofdream.domain.model.ProductionRecord
import com.example.loafofdream.presentation.viewmodels.ProductListState
import com.example.loafofdream.presentation.viewmodels.ProductionViewModel
import com.example.loafofdream.presentation.viewmodels.ProductViewModel
import com.example.loafofdream.presentation.viewmodels.SelectedDateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductionScreen(
    selectedDateViewModel: SelectedDateViewModel,
    productViewModel: ProductViewModel,
    productionViewModel: ProductionViewModel,
    onBack: () -> Unit
) {
    val date by selectedDateViewModel.selectedDate.collectAsState()
    val dateStr = date.toString()

    val productListState by productViewModel.listState.collectAsState()
    val records by productionViewModel.records.collectAsState()
    val isLoading by productionViewModel.isLoading.collectAsState()
    val result by productionViewModel.result.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var recordToDelete by remember { mutableStateOf<ProductionRecord?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(result) {
        result?.let { snackbarHostState.showSnackbar(it); productionViewModel.clearResult() }
    }

    LaunchedEffect(dateStr) {
        productViewModel.loadProducts()
        productionViewModel.loadRecords(dateStr)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Производство на $dateStr") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (records.isNotEmpty()) {
                Text("Произведено за $dateStr:", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(records) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { recordToDelete = record }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(record.productName, fontWeight = FontWeight.SemiBold)
                                Text("+${record.quantity} шт", color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Нет записей за $dateStr", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            val products = (productListState as? ProductListState.Success)?.products ?: emptyList()
            if (products.isNotEmpty()) {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                ) {
                    Text("Добавить выработку")
                }
            }
        }
    }

    if (showDialog) {
        val products = (productListState as? ProductListState.Success)?.products ?: emptyList()
        ProductionEntryDialog(
            products = products,
            onDismiss = { showDialog = false },
            onConfirm = { productId, quantity ->
                productionViewModel.addProduction(productId, quantity, dateStr)
                showDialog = false
            }
        )
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Удалить запись?") },
            text = { Text("${record.productName}: +${record.quantity} шт") },
            confirmButton = {
                TextButton(onClick = {
                    productionViewModel.deleteProduction(record.id, dateStr)
                    recordToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductionEntryDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantity by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить выработку") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Продукт") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        products.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = { selectedProduct = p; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = quantity, onValueChange = { quantity = it },
                    label = { Text("Количество (шт)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val id = selectedProduct?.id ?: return@TextButton
                val qty = quantity.toIntOrNull() ?: return@TextButton
                onConfirm(id, qty)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
