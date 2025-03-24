package ru.cororo.corasense.repo.image

import org.jetbrains.exposed.dao.id.UUIDTable

object ImageTable : UUIDTable("images") {
    val name = varchar("name", 256)
}