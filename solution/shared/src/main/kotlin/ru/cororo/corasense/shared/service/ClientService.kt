package ru.cororo.corasense.shared.service

import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.shared.util.UuidString

@Rpc
interface ClientService {
    suspend fun getClient(id: UuidString): Client?

    suspend fun saveClient(client: Client)

    suspend fun saveClients(clients: Iterable<Client>)

    suspend fun findClientsByLogin(name: String): Set<Client>
}