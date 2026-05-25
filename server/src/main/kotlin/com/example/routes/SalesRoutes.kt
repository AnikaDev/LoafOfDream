package com.example.routes

import com.example.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.salesRoutes() {
    authenticate("jwt-auth") {
        route("/sales") {
            post {
                val req = call.receive<SaleRequest>()
                for (item in req.items) {
                    val product = transaction {
                        Products.select { Products.id eq item.productId }.firstOrNull()
                    }
                    if (product == null) {
                        call.respond(HttpStatusCode.NotFound, MessageResponse("Продукт ${item.productId} не найден"))
                        return@post
                    }
                    if (product[Products.quantity] < item.quantity) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            MessageResponse("Недостаточно ${product[Products.name]} на складе")
                        )
                        return@post
                    }
                }
                transaction {
                    for (item in req.items) {
                        val product = Products.select { Products.id eq item.productId }.first()
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
