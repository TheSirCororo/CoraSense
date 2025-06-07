package ru.cororo.corasense.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.cororo.corasense.repo.client.ClientRepo
import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.shared.service.ClientService
import java.util.*
import kotlin.coroutines.CoroutineContext

class ClientServiceImpl(private val clientRepo: ClientRepo) : ClientService, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("ClientServiceImpl")

    override suspend fun getClient(id: UUID) = clientRepo.get(id)

    override suspend fun saveClient(client: Client) = clientRepo.save(client)

    override suspend fun saveClients(clients: Iterable<Client>) = clientRepo.saveAll(clients)

    override suspend fun findClientsByLogin(name: String) = clientRepo.findByLogin(name)
}