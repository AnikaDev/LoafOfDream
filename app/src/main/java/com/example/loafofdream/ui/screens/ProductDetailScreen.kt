package com.example.loafofdream.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.loafofdream.domain.model.ProductRequest
import com.example.loafofdream.presentation.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    viewModel: ProductViewModel,
    userRole: String,
    onBack: () -> Unit
) {
    val product by viewModel.selectedProduct.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) { viewModel.loadProduct(productId) }

    LaunchedEffect(actionResult) {
        if (actionResult == "Продукт удалён") onBack()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionResult) {
        actionResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Продукт") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    if (userRole == "owner") {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        product?.let { p ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!p.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = p.photoUrl,
                        contentDescription = p.name,
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(p.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(p.category, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    DetailRow("Цена", "%.2f ₽".format(p.price))
                    DetailRow("В наличии", "${p.quantity} шт")
                    DetailRow("Срок реализации", "${p.shelfLifeHours} ч")
                    if (!p.lastProducedAt.isNullOrBlank()) {
                        DetailRow("Последнее производство", p.lastProducedAt.take(16).replace("T", " "))
                    }
                }
            }
        } ?: Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (showEditDialog) {
        product?.let { p ->
            EditProductDialog(
                initialName = p.name,
                initialCategory = p.category,
                initialPrice = p.price.toString(),
                initialShelf = p.shelfLifeHours.toString(),
                initialPhotoUrl = p.photoUrl ?: "",
                onDismiss = { showEditDialog = false },
                onConfirm = { name, category, price, shelf, photoUrl ->
                    viewModel.updateProduct(p.id, ProductRequest(
                        name, category, price, p.quantity, shelf, photoUrl.ifBlank { null }
                    ))
                    showEditDialog = false
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить продукт?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(productId)
                    showDeleteDialog = false
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun EditProductDialog(
    initialName: String,
    initialCategory: String,
    initialPrice: String,
    initialShelf: String,
    initialPhotoUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var price by remember { mutableStateOf(initialPrice) }
    var shelf by remember { mutableStateOf(initialShelf) }
    var photoUrl by remember { mutableStateOf(initialPhotoUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать продукт") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it },
                    label = { Text("Категория") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text("Цена (₽)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = shelf, onValueChange = { shelf = it },
                    label = { Text("Срок реализации (часов)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it },
                    label = { Text("URL фото") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(name, category, price.toDoubleOrNull() ?: 0.0,
                    shelf.toIntOrNull() ?: 24, photoUrl)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
