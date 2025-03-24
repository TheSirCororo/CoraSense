package ru.cororo.corasense.repo.moderation

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import ru.cororo.corasense.plugin.sql
import ru.cororo.corasense.util.deleteById
import ru.cororo.corasense.util.getById

object DatabaseBlockedWordRepo : BlockedWordRepo {
    override suspend fun getAll(): Set<String> = sql {
        BlockedWordTable.selectAll().mapTo(mutableSetOf()) { it[BlockedWordTable.id].value }
    }

    override suspend fun String.getId(): String = this

    override suspend fun save(entity: String): Unit = sql {
        BlockedWordTable.insert {
            it[id] = entity
        }
    }

    override suspend fun saveAll(entities: Iterable<String>) = sql {
        entities.forEach {
            save(it)
        }
    }

    override suspend fun delete(id: String): Unit = sql {
        BlockedWordTable.deleteById(id)
    }

    override suspend fun get(id: String): String? = BlockedWordTable.getById(id)?.get(BlockedWordTable.id)?.value
}