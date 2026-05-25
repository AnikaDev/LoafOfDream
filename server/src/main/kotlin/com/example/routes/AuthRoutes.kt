package com.example.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Route.authRoutes(jwtSecret: String, jwtIssuer: String, jwtAudience: String) {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            if (req.role != "owner" && req.role != "seller") {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("Роль должна быть owner или seller"))
                return@post
            }
            val hash = BCrypt.withDefaults().hashToString(12, req.password.toCharArray())
            try {
                transaction {
                    Users.insert {
                        it[name] = req.name
                        it[email] = req.email
                        it[passwordHash] = hash
                        it[role] = req.role
                    }
                }
                call.respond(HttpStatusCode.Created, MessageResponse("Пользователь зарегистрирован"))
            } catch (e: ExposedSQLException) {
                call.respond(HttpStatusCode.Conflict, MessageResponse("Email уже используется"))
            }
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val user = transaction {
                Users.select { Users.email eq req.email }.firstOrNull()
            }
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, MessageResponse("Неверный email или пароль"))
                return@post
            }
            val verified = BCrypt.verifyer().verify(req.password.toCharArray(), user[Users.passwordHash]).verified
            if (!verified) {
                call.respond(HttpStatusCode.Unauthorized, MessageResponse("Неверный email или пароль"))
                return@post
            }
            val token = JWT.create()
                .withIssuer(jwtIssuer)
                .withAudience(jwtAudience)
                .withClaim("userId", user[Users.id])
                .withClaim("role", user[Users.role])
                .withExpiresAt(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L))
                .sign(Algorithm.HMAC256(jwtSecret))
            call.respond(AuthResponse(token, user[Users.role], user[Users.name], user[Users.id]))
        }
    }
}
