package ru.cororo.corasense.repo.client

import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.repo.CachedCrudRepo
import java.util.*

class CachedClientRepo(backedRepo: ClientRepo) : CachedCrudRepo<UUID, Client>(backedRepo), ClientRepo {
    private val clientsByLogin = newCache<String, MutableSet<Client>> {
        backedRepo.findByLogin(it).toMutableSet()
    }

    override suspend fun save(entity: Client) {
        super.save(entity)
        clientsByLogin.invalidate(entity.login)
    }

    override suspend fun saveAll(entities: Iterable<Client>) {
        super.saveAll(entities)
        entities.forEach {
            clientsByLogin.invalidate(it.login)
        }
    }

    override suspend fun delete(id: UUID) {
        val client = get(id) ?: return
        super.delete(id)
        clientsByLogin.invalidate(client.login)
    }

    override suspend fun findByLogin(name: String): Set<Client> = clientsByLogin.get(name)
}