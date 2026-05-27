package com.example.loafofdream.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loafofdream.presentation.viewmodels.CalendarState
import com.example.loafofdream.presentation.viewmodels.CalendarViewModel
import com.example.loafofdream.presentation.viewmodels.SelectedDateViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ruMonths = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    selectedDateViewModel: SelectedDateViewModel,
    onBack: () -> Unit
) {
    val calendarState by viewModel.state.collectAsState()
    val globalDate by selectedDateViewModel.selectedDate.collectAsState()

    var localDate by remember { mutableStateOf(globalDate) }
    var displayMonth by remember { mutableStateOf(globalDate.withDayOfMonth(1)) }
    var dialogDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(globalDate) {
        localDate = globalDate
    }

    LaunchedEffect(localDate) { viewModel.loadStats(localDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Календарь продаж") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { displayMonth = displayMonth.minusMonths(1) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, null)
                }
                Text(
                    "${ruMonths[displayMonth.monthValue - 1]} ${displayMonth.year}",
                    fontWeight = FontWeight.Bold, fontSize = 18.sp
                )
                IconButton(onClick = { displayMonth = displayMonth.plusMonths(1) }) {
                    Icon(Icons.Default.KeyboardArrowRight, null)
                }
            }

            val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                daysOfWeek.forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            val firstDay = displayMonth
            val firstDayOfWeek = firstDay.dayOfWeek.value
            val daysInMonth = firstDay.lengthOfMonth()
            val cells = (1 - firstDayOfWeek + 1)..(daysInMonth)
            val weeks = ((firstDayOfWeek - 1 + daysInMonth - 1) / 7) + 1

            for (week in 0 until weeks) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    for (dow in 1..7) {
                        val dayNum = week * 7 + dow - (firstDayOfWeek - 1)
                        if (dayNum < 1 || dayNum > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date = displayMonth.withDayOfMonth(dayNum)
                            val isLocalSelected = date == localDate
                            val isGlobalSelected = date == globalDate && date != localDate
                            val isToday = date == LocalDate.now()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isLocalSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        }
                                    )
                                    .combinedClickable(
                                        onClick = { localDate = date },
                                        onLongClick = { dialogDate = date }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$dayNum",
                                        color = when {
                                            isLocalSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = if (isLocalSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isGlobalSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            when (val state = calendarState) {
                is CalendarState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CalendarState.Success -> {
                    val s = state.stats
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))),
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatChip("День", "%.0f ₽".format(s.dayRevenue), modifier = Modifier.weight(1f))
                            StatChip("Месяц", "%.0f ₽".format(s.monthRevenue), modifier = Modifier.weight(1f))
                            StatChip("Год", "%.0f ₽".format(s.yearRevenue), modifier = Modifier.weight(1f))
                        }
                    }
                }
                is CalendarState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp))
                }
                else -> Unit
            }
        }
    }

    dialogDate?.let { date ->
        AlertDialog(
            onDismissRequest = { dialogDate = null },
            title = { Text("Выбрать дату?") },
            text = { Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))) },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateViewModel.setDate(date)
                    localDate = date
                    dialogDate = null
                }) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { dialogDate = null }) { Text("Нет") }
            }
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}
