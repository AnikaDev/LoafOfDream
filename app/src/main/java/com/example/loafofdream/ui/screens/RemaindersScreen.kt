package com.example.loafofdream.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.loafofdream.presentation.theme.ExpiredRed
import com.example.loafofdream.presentation.theme.SuccessGreen
import com.example.loafofdream.presentation.viewmodels.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemaindersScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit
) {
    val remainders by viewModel.remainders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadRemainders() }
    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Остатки") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (remainders.none { it.quantity > 0 }) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Нет данных об остатках", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            } else {
                val expired = remainders.filter { it.isExpired && it.quantity > 0 }
                val normal = remainders.filter { !it.isExpired && it.quantity > 0 }

                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    if (expired.isNotEmpty()) {
                        item {
                            Text("Просроченные", fontWeight = FontWeight.Bold,
                                color = ExpiredRed, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(expired) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = ExpiredRed.copy(alpha = 0.1f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.SemiBold)
                                        Text(item.category, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Surface(color = ExpiredRed, shape = MaterialTheme.shapes.small) {
                                            Text("ПРОСРОЧЕНО", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = androidx.compose.ui.graphics.Color.White)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${item.quantity} шт", fontWeight = FontWeight.Bold, color = ExpiredRed)
                                    }
                                }
                            }
                        }
                    }

                    if (normal.isNotEmpty()) {
                        item {
                            Text("В наличии", fontWeight = FontWeight.Bold,
                                color = SuccessGreen, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(normal) { item ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.SemiBold)
                                        Text(item.category, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Text("%.2f ₽/шт".format(item.price), style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("${item.quantity} шт", fontWeight = FontWeight.Bold,
                                        color = if (item.quantity > 0) SuccessGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
