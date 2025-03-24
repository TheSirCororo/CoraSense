package ru.cororo.corasense.repo.moderation

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object BlockedWordTable : IdTable<String>("blocked_words") {
    override val id: Column<EntityID<String>> = varchar("id", 32).uniqueIndex().entityId()
}
