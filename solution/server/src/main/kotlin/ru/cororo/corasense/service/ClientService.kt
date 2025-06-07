package ru.cororo.corasense.service

import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.repo.client.ClientRepo
import java.util.UUID

class ClientService(private val clientRepo: ClientRepo) {
    suspend fun getClient(id: UUID) = clientRepo.get(id)

    suspend fun saveClient(client: Client) = clientRepo.save(client)

    suspend fun saveClients(iterable: Iterable<Client>) = clientRepo.saveAll(iterable)

    suspend fun findClientsByLogin(name: String) = clientRepo.findByLogin(name)
}