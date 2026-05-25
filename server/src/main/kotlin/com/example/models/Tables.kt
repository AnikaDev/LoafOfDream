package com.example.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 200)
    val role = varchar("role", 20)
    override val primaryKey = PrimaryKey(id)
}

object Products : Table("products") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 200)
    val category = varchar("category", 100)
    val price = decimal("price", 10, 2)
    val quantity = integer("quantity").default(0)
    val shelfLifeHours = integer("shelf_life_hours")
    val photoUrl = varchar("photo_url", 500).nullable()
    val lastProducedAt = datetime("last_produced_at").nullable()
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ProductionRecords : Table("production_records") {
    val id = integer("id").autoIncrement()
    val productId = integer("product_id").references(Products.id)
    val quantity = integer("quantity")
    val date = varchar("date", 10)
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Sales : Table("sales") {
    val id = integer("id").autoIncrement()
    val productId = integer("product_id").references(Products.id)
    val quantity = integer("quantity")
    val priceAtTime = decimal("price_at_time", 10, 2)
    val date = varchar("date", 10)
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}
