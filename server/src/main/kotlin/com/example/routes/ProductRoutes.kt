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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

fun Route.productRoutes() {
    route("/products") {
        get {
            val search = call.request.queryParameters["q"]
            val category = call.request.queryParameters["category"]
            val products = transaction {
                var query = Products.selectAll()
                if (!search.isNullOrBlank()) {
                    query = query.andWhere { Products.name.lowerCase() like "%${search.lowercase()}%" }
                }
                if (!category.isNullOrBlank()) {
                    query = query.andWhere { Products.category eq category }
                }
                query.map { toProductDto(it) }
            }
            call.respond(products)
        }

        get("/{id}") {
            val pid = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("Неверный id"))
            val product = transaction {
                Products.select { Products.id eq pid }.firstOrNull()?.let { toProductDto(it) }
            }
            if (product == null) call.respond(HttpStatusCode.NotFound, MessageResponse("Продукт не найден"))
            else call.respond(product)
        }

        authenticate("jwt-auth") {
            post {
                requireOwner(call) ?: return@post
                val req = call.receive<CreateProductRequest>()
                val newId = transaction {
                    Products.insert {
                        it[name] = req.name
                        it[category] = req.category
                        it[price] = BigDecimal.valueOf(req.price)
                        it[quantity] = req.quantity
                        it[shelfLifeHours] = req.shelfLifeHours
                        it[photoUrl] = req.photoUrl
                        it[createdAt] = LocalDateTime.now()
                    } get Products.id
                }
                call.respond(HttpStatusCode.Created, IdResponse(newId))
            }

            put("/{id}") {
                requireOwner(call) ?: return@put
                val pid = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, MessageResponse("Неверный id"))
                val req = call.receive<CreateProductRequest>()
                transaction {
                    Products.update({ Products.id eq pid }) {
                        it[name] = req.name
                        it[category] = req.category
                        it[price] = BigDecimal.valueOf(req.price)
                        it[quantity] = req.quantity
                        it[shelfLifeHours] = req.shelfLifeHours
                        it[photoUrl] = req.photoUrl
                    }
                }
                call.respond(HttpStatusCode.OK, MessageResponse("Обновлено"))
            }

            delete("/{id}") {
                requireOwner(call) ?: return@delete
                val pid = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse("Неверный id"))
                val deleted = transaction {
                    if (Products.select { Products.id eq pid }.firstOrNull() == null) return@transaction false
                    Sales.deleteWhere { Sales.productId eq pid }
                    ProductionRecords.deleteWhere { ProductionRecords.productId eq pid }
                    Products.deleteWhere { Products.id eq pid }
                    true
                }
                if (deleted) call.respond(HttpStatusCode.OK, MessageResponse("Продукт удалён"))
                else call.respond(HttpStatusCode.NotFound, MessageResponse("Продукт не найден"))
            }
        }
    }
}

private fun toProductDto(row: ResultRow) = ProductDto(
    id = row[Products.id],
    name = row[Products.name],
    category = row[Products.category],
    price = row[Products.price].toDouble(),
    quantity = row[Products.quantity],
    shelfLifeHours = row[Products.shelfLifeHours],
    photoUrl = row[Products.photoUrl],
    lastProducedAt = row[Products.lastProducedAt]?.toString()
)

private suspend fun requireOwner(call: ApplicationCall): Unit? {
    val role = call.principal<JWTPrincipal>()?.getClaim("role", String::class)
    return if (role == "owner") Unit
    else {
        call.respond(HttpStatusCode.Forbidden, MessageResponse("Доступ только для владельца"))
        null
    }
}
