package ru.cororo.corasense.repo.client

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.plugin.sql
import ru.cororo.corasense.util.deleteById
import ru.cororo.corasense.util.getById
import java.util.*

object DatabaseClientRepo : ClientRepo {
    override suspend fun Client.getId(): UUID = id

    override suspend fun save(entity: Client): Unit = sql {
        ClientTable.upsert(ClientTable.id, onUpdate = {
            with(ClientTable) {
                it[login] = entity.login
                it[age] = entity.age
                it[location] = entity.location
                it[gender] = entity.gender
            }
        }) {
            it[id] = entity.id
            it[login] = entity.login
            it[age] = entity.age
            it[location] = entity.location
            it[gender] = entity.gender
        }
    }

    override suspend fun saveAll(entities: Iterable<Client>) = sql {
        entities.forEach { save(it) }
    }

    override suspend fun delete(id: UUID): Unit = sql {
        ClientTable.deleteById(id)
    }

    override suspend fun get(id: UUID): Client? = sql {
        ClientTable.getById(id)?.asClient()
    }

    private fun ResultRow.asClient() =
        Client(
            this[ClientTable.id].value,
            this[ClientTable.login],
            this[ClientTable.age],
            this[ClientTable.location],
            this[ClientTable.gender]
        )

    override suspend fun findByLogin(name: String): Set<Client> = sql {
        ClientTable.selectAll().where { ClientTable.login eq name }.mapTo(mutableSetOf()) { it.asClient() }
    }
}