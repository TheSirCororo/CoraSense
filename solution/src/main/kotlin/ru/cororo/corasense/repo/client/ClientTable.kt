package ru.cororo.corasense.repo.client

import org.jetbrains.exposed.dao.id.UUIDTable
import ru.cororo.corasense.model.client.data.Client

object ClientTable : UUIDTable("clients") {
    val login = varchar("login", 128)
    val age = integer("age")
    val location = varchar("location", 256)
    val gender = enumerationByName<Client.Gender>("gender", 6)
}
