package ru.cororo.corasense.repo.advertiser

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.plugin.sql
import ru.cororo.corasense.util.deleteById
import ru.cororo.corasense.util.getById
import java.util.*

object DatabaseAdvertiserRepo : AdvertiserRepo {
    override suspend fun Advertiser.getId(): UUID = id

    override suspend fun save(entity: Advertiser): Unit = sql {
        AdvertiserTable.upsert(AdvertiserTable.id, onUpdate = {
            it[AdvertiserTable.name] = entity.name
        }) {
            it[id] = entity.id
            it[name] = entity.name
        }
    }

    override suspend fun saveAll(entities: Iterable<Advertiser>) = sql {
        entities.forEach { save(it) }
    }

    override suspend fun delete(id: UUID): Unit = sql {
        AdvertiserTable.deleteById(id)
    }

    private fun ResultRow.asAdvertiser() = Advertiser(
        this[AdvertiserTable.id].value, this[AdvertiserTable.name]
    )

    override suspend fun get(id: UUID): Advertiser? = sql {
        AdvertiserTable.getById(id)?.asAdvertiser()
    }

    override suspend fun getAll(): List<Advertiser> = sql {
        AdvertiserTable.selectAll().map { it.asAdvertiser() }
    }

    override suspend fun findByName(name: String): List<Advertiser> = sql {
        AdvertiserTable.selectAll().where { AdvertiserTable.name.lowerCase() eq name.lowercase() }
            .map { it.asAdvertiser() }
    }
}