package com.example.loafofdream.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loafofdream.data.local.SearchHistoryManager
import com.example.loafofdream.domain.model.Product
import com.example.loafofdream.domain.model.ProductRequest
import com.example.loafofdream.presentation.components.SearchBar
import com.example.loafofdream.presentation.viewmodels.ProductListState
import com.example.loafofdream.presentation.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    searchHistoryManager: SearchHistoryManager,
    userRole: String,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToProduction: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToRemainders: () -> Unit,
    onNavigateToRevenue: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onLogout: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(searchHistoryManager.getHistory()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val listState by viewModel.listState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Продукция", fontWeight = FontWeight.Bold) },
                actions = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeToggle() },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Производство") },
                            onClick = { showMenu = false; onNavigateToProduction() },
                            leadingIcon = { Icon(Icons.Default.Build, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Продажи") },
                            onClick = { showMenu = false; onNavigateToSales() },
                            leadingIcon = { Icon(Icons.Default.ShoppingCart, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Остатки") },
                            onClick = { showMenu = false; onNavigateToRemainders() },
                            leadingIcon = { Icon(Icons.Default.List, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Выручка") },
                            onClick = { showMenu = false; onNavigateToRevenue() },
                            leadingIcon = { Icon(Icons.Default.Star, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Календарь") },
                            onClick = { showMenu = false; onNavigateToCalendar() },
                            leadingIcon = { Icon(Icons.Default.DateRange, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Выйти") },
                            onClick = { showMenu = false; onLogout() },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (userRole == "owner") {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить продукт")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { q ->
                        if (q.isNotBlank()) {
                            searchHistoryManager.addToHistory(q)
                            history = searchHistoryManager.getHistory()
                        }
                        viewModel.loadProducts(query = q.ifBlank { null })
                        showHistory = false
                    },
                    onFocused = { showHistory = true }
                )

                if (showHistory && history.isNotEmpty() && searchQuery.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("История поиска", style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp))
                                TextButton(onClick = {
                                    searchHistoryManager.clearHistory()
                                    history = emptyList()
                                    showHistory = false
                                }) {
                                    Text("Очистить историю")
                                }
                            }
                            history.forEach { item ->
                                ListItem(
                                    headlineContent = { Text(item) },
                                    leadingContent = { Icon(Icons.Default.Refresh, null) },
                                    modifier = Modifier.clickable {
                                        searchQuery = item
                                        viewModel.loadProducts(query = item)
                                        showHistory = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            when (val state = listState) {
                is ProductListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProductListState.Success -> {
                    showHistory = false
                    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                        items(state.products) { product ->
                            ProductCard(
                                product = product,
                                onClick = {
                                    searchHistoryManager.addToHistory(product.name)
                                    history = searchHistoryManager.getHistory()
                                    onNavigateToDetail(product.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                is ProductListState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ничего не найдено", style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                }
                is ProductListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.message, style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.retryLastLoad() }) {
                                Text("Обновить")
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, price, qty, shelf, photoUrl ->
                viewModel.createProduct(
                    ProductRequest(name, category, price, qty, shelf, photoUrl.ifBlank { null })
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(product.category, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Text("%.2f ₽".format(product.price), color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (product.quantity > 0) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${product.quantity} шт",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (product.quantity > 0) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Int, Int, String) -> Unit
) {
    val categories = listOf("Выпечка", "Торты и пирожные", "Горячие напитки", "Холодные напитки", "Прочее")
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("0") }
    var shelf by remember { mutableStateOf("24") }
    var photoUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый продукт") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Категория") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { category = cat; categoryExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text("Цена (₽)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = shelf, onValueChange = { shelf = it },
                    label = { Text("Срок реализации (часов)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it },
                    label = { Text("URL фото (необязательно)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    name, category,
                    price.toDoubleOrNull() ?: 0.0,
                    qty.toIntOrNull() ?: 0,
                    shelf.toIntOrNull() ?: 24,
                    photoUrl
                )
            }) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
