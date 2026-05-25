package com.example.routes

import com.example.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.productionRoutes() {
    authenticate("jwt-auth") {
        route("/production") {
            post {
                val role = call.principal<JWTPrincipal>()?.getClaim("role", String::class)
                if (role != "owner") {
                    call.respond(HttpStatusCode.Forbidden, MessageResponse("Доступ только для владельца"))
                    return@post
                }
                val req = call.receive<ProductionRequest>()
                val exists = transaction { Products.select { Products.id eq req.productId }.count() > 0 }
                if (!exists) {
                    call.respond(HttpStatusCode.NotFound, MessageResponse("Продукт не найден"))
                    return@post
                }
                transaction {
                    ProductionRecords.insert {
                        it[productId] = req.productId
                        it[quantity] = req.quantity
                        it[date] = req.date
                        it[createdAt] = LocalDateTime.now()
                    }
                    Products.update({ Products.id eq req.productId }) {
                        with(SqlExpressionBuilder) {
                            it.update(quantity, quantity + req.quantity)
                        }
                        it[lastProducedAt] = LocalDateTime.now()
                    }
                }
                call.respond(HttpStatusCode.Created, MessageResponse("Производство зафиксировано"))
            }

            get {
                val date = call.request.queryParameters["date"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Укажите дату"))
                val records = transaction {
                    (ProductionRecords innerJoin Products)
                        .select { ProductionRecords.date eq date }
                        .map {
                            ProductionRecordDto(
                                id = it[ProductionRecords.id],
                                productId = it[ProductionRecords.productId],
                                productName = it[Products.name],
                                quantity = it[ProductionRecords.quantity],
                                date = it[ProductionRecords.date]
                            )
                        }
                }
                call.respond(records)
            }
        }
    }
}
