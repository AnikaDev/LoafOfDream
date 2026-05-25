package com.example.routes

import com.example.models.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

fun Route.statsRoutes() {
    authenticate("jwt-auth") {
        get("/remainders") {
            val now = LocalDateTime.now()
            val items = transaction {
                Products.selectAll().map { row ->
                    val lastProduced = row[Products.lastProducedAt]
                    val shelfLife = row[Products.shelfLifeHours]
                    val isExpired = if (lastProduced != null && row[Products.quantity] > 0) {
                        lastProduced.plusHours(shelfLife.toLong()).isBefore(now)
                    } else false
                    RemainderDto(
                        productId = row[Products.id],
                        productName = row[Products.name],
                        category = row[Products.category],
                        quantity = row[Products.quantity],
                        price = row[Products.price].toDouble(),
                        isExpired = isExpired,
                        photoUrl = row[Products.photoUrl]
                    )
                }
            }
            call.respond(items)
        }

        get("/stats/revenue") {
            val period = call.request.queryParameters["period"] ?: "day"
            val dateParam = call.request.queryParameters["date"]
            val refDate = if (dateParam != null) runCatching { LocalDate.parse(dateParam) }.getOrNull()
                          else LocalDate.now()
            val today = refDate ?: LocalDate.now()
            val startDate = when (period) {
                "week"  -> today.minusDays(6)
                "month" -> today.withDayOfMonth(1)
                "year"  -> today.withDayOfYear(1)
                else    -> today
            }
            val result = transaction {
                val rows = Sales.select {
                    Sales.date greaterEq startDate.toString() and (Sales.date lessEq today.toString())
                }
                val total = rows.sumOf { it[Sales.quantity] * it[Sales.priceAtTime].toDouble() }
                RevenueResponse(period, total, rows.count().toInt())
            }
            call.respond(result)
        }

        get("/stats/calendar") {
            val dateStr = call.request.queryParameters["date"] ?: LocalDate.now().toString()
            val date = runCatching { LocalDate.parse(dateStr) }.getOrElse { LocalDate.now() }

            val monthStart = date.withDayOfMonth(1).toString()
            val monthEnd   = date.withDayOfMonth(date.lengthOfMonth()).toString()
            val yearStart  = date.withDayOfYear(1).toString()
            val yearEnd    = date.withDayOfYear(date.lengthOfYear()).toString()

            val result = transaction {
                val dayRows = (Sales innerJoin Products)
                    .select { Sales.date eq dateStr }
                    .map {
                        SaleRecordDto(
                            id = it[Sales.id],
                            productId = it[Sales.productId],
                            productName = it[Products.name],
                            quantity = it[Sales.quantity],
                            priceAtTime = it[Sales.priceAtTime].toDouble(),
                            date = it[Sales.date]
                        )
                    }

                val monthRows = Sales.select { Sales.date greaterEq monthStart and (Sales.date lessEq monthEnd) }
                val yearRows  = Sales.select { Sales.date greaterEq yearStart  and (Sales.date lessEq yearEnd) }

                CalendarDayStats(
                    date = dateStr,
                    sales = dayRows,
                    dayRevenue  = dayRows.sumOf  { it.quantity * it.priceAtTime },
                    dayCount    = dayRows.size,
                    monthRevenue = monthRows.sumOf { it[Sales.quantity] * it[Sales.priceAtTime].toDouble() },
                    monthCount   = monthRows.count().toInt(),
                    yearRevenue  = yearRows.sumOf  { it[Sales.quantity] * it[Sales.priceAtTime].toDouble() },
                    yearCount    = yearRows.count().toInt()
                )
            }
            call.respond(result)
        }
    }
}
