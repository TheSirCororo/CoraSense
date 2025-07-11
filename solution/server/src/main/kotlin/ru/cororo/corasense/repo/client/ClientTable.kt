package ru.cororo.corasense.repo.client

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import ru.cororo.corasense.shared.model.client.Client

object ClientTable : UUIDTable("clients") {
    val login = varchar("login", 128)
    val age = integer("age")
    val location = varchar("location", 256)
    val gender = enumerationByName<Client.Gender>("gender", 6)
}
