package com.example.loafofdream.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.loafofdream.domain.model.Product
import com.example.loafofdream.domain.model.SaleItem
import com.example.loafofdream.presentation.viewmodels.ProductListState
import com.example.loafofdream.presentation.viewmodels.ProductViewModel
import com.example.loafofdream.presentation.viewmodels.SalesViewModel
import com.example.loafofdream.presentation.viewmodels.SelectedDateViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SalesScreen(
    selectedDateViewModel: SelectedDateViewModel,
    productViewModel: ProductViewModel,
    salesViewModel: SalesViewModel,
    onBack: () -> Unit
) {
    val date by selectedDateViewModel.selectedDate.collectAsState()
    val dateStr = date.toString()

    val productListState by productViewModel.listState.collectAsState()
    val salesRecords by salesViewModel.records.collectAsState()
    val isLoading by salesViewModel.isLoading.collectAsState()
    val result by salesViewModel.result.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<com.example.loafofdream.domain.model.SaleRecord?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(result) {
        result?.let {
            snackbarHostState.showSnackbar(it)
            salesViewModel.clearResult()
        }
    }

    LaunchedEffect(dateStr) {
        productViewModel.loadProducts()
        salesViewModel.loadSales(dateStr)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Продажи на $dateStr") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            if (salesRecords.isNotEmpty()) {
                val totalRevenue = salesRecords.sumOf { it.quantity * it.priceAtTime }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(salesRecords) { rec ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { recordToDelete = rec }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(rec.productName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${rec.quantity} шт × %.2f ₽".format(rec.priceAtTime),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    "%.2f ₽".format(rec.quantity * rec.priceAtTime),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Итого:", fontWeight = FontWeight.Bold)
                            Text(
                                "%.2f ₽".format(totalRevenue),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Нет продаж за $dateStr",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Button(
                onClick = { showAddItemDialog = true },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
            ) {
                Text("Добавить позицию")
            }
        }
    }

    recordToDelete?.let { rec ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Удалить запись?") },
            text = { Text("${rec.productName}, ${rec.quantity} шт — %.2f ₽".format(rec.quantity * rec.priceAtTime)) },
            confirmButton = {
                TextButton(onClick = {
                    salesViewModel.deleteSale(rec.id, dateStr)
                    recordToDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) { Text("Отмена") }
            }
        )
    }

    if (showAddItemDialog) {
        val products = (productListState as? ProductListState.Success)?.products ?: emptyList()
        SaleItemDialog(
            products = products.filter { it.quantity > 0 },
            onDismiss = { showAddItemDialog = false },
            onConfirm = { productId, quantity ->
                salesViewModel.addSales(listOf(SaleItem(productId, quantity)), dateStr)
                showAddItemDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleItemDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantity by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить позицию") },
        text = {
            if (products.isEmpty()) {
                Text("Нет доступных товаров в наличии")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = selectedProduct?.let { "${it.name} (${it.quantity} шт)" } ?: "",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Продукт") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (${p.quantity} шт)") },
                                    onClick = { selectedProduct = p; expanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it },
                        label = { Text("Количество") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val id = selectedProduct?.id ?: return@TextButton
                val qty = quantity.toIntOrNull() ?: return@TextButton
                if (qty > 0) onConfirm(id, qty)
            }) { Text("Добавить товар") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
