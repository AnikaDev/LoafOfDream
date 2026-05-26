package com.example.routes

import com.example.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.salesRoutes() {
    authenticate("jwt-auth") {
        route("/sales") {
            post {
                val req = call.receive<SaleRequest>()
                var errorStatus: HttpStatusCode? = null
                var errorMessage: String? = null
                try {
                    transaction {
                        for (item in req.items) {
                            val product = Products.select { Products.id eq item.productId }.firstOrNull()
                            if (product == null) {
                                errorStatus = HttpStatusCode.NotFound
                                errorMessage = "Продукт ${item.productId} не найден"
                                throw IllegalArgumentException(errorMessage)
                            }
                            if (product[Products.quantity] < item.quantity) {
                                errorStatus = HttpStatusCode.BadRequest
                                errorMessage = "Недостаточно ${product[Products.name]} на складе"
                                throw IllegalStateException(errorMessage)
                            }
                            Sales.insert {
                                it[productId] = item.productId
                                it[quantity] = item.quantity
                                it[priceAtTime] = product[Products.price]
                                it[date] = req.date
                                it[createdAt] = LocalDateTime.now()
                            }
                            Products.update({ Products.id eq item.productId }) {
                                with(SqlExpressionBuilder) {
                                    it.update(quantity, quantity - item.quantity)
                                }
                            }
                        }
                    }
                    call.respond(HttpStatusCode.Created, MessageResponse("Продажи зафиксированы"))
                } catch (e: Exception) {
                    call.respond(errorStatus ?: HttpStatusCode.BadRequest, MessageResponse(errorMessage ?: "Ошибка: проверьте наличие товара"))
                }
            }

            delete("/{id}") {
                val recordId = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse("Неверный id"))
                val deleted = transaction {
                    val row = Sales.select { Sales.id eq recordId }.firstOrNull()
                        ?: return@transaction false
                    val qty = row[Sales.quantity]
                    val pid = row[Sales.productId]
                    Sales.deleteWhere { Sales.id eq recordId }
                    Products.update({ Products.id eq pid }) {
                        with(SqlExpressionBuilder) {
                            it.update(Products.quantity, Products.quantity + qty)
                        }
                    }
                    true
                }
                if (deleted) call.respond(HttpStatusCode.OK, MessageResponse("Запись удалена"))
                else call.respond(HttpStatusCode.NotFound, MessageResponse("Запись не найдена"))
            }

            get {
                val date = call.request.queryParameters["date"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Укажите дату"))
                val records = transaction {
                    (Sales innerJoin Products)
                        .select { Sales.date eq date }
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
                }
                call.respond(records)
            }
        }
    }
}
