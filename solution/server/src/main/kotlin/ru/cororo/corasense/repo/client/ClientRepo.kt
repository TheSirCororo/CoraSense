package ru.cororo.corasense.repo.client

import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.repo.CrudRepo
import java.util.UUID

interface ClientRepo : CrudRepo<UUID, Client> {
    suspend fun findByLogin(name: String): Set<Client>
}