package ru.cororo.corasense.repo.moderation

import org.jetbrains.exposed.v1.core.dao.id.IdTable

object BlockedWordTable : IdTable<String>("blocked_words") {
    override val id = varchar("id", 32).uniqueIndex().entityId()
}
