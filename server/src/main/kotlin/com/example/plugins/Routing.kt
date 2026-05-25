package com.example.plugins

import com.example.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()

    routing {
        authRoutes(jwtSecret, jwtIssuer, jwtAudience)
        productRoutes()
        productionRoutes()
        salesRoutes()
        statsRoutes()
    }
}
