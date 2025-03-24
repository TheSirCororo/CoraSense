package ru.cororo.corasense.repo.moderation

import ru.cororo.corasense.repo.CachedCrudRepo
import kotlin.time.Duration.Companion.seconds

class CachedBlockedWordRepo(backedRepo: BlockedWordRepo) : CachedCrudRepo<String, String>(backedRepo, expiration = Int.MAX_VALUE.seconds), BlockedWordRepo {
    private var initialized = false

    override suspend fun getAll(): Set<String> {
        return if (initialized) {
            cacheById.asMap().values.filterNotNullTo(mutableSetOf())
        } else {
            initialized = true
            val allValues = (backedRepo as BlockedWordRepo).getAll()
            for (word in allValues) {
                cacheById[word] = word
            }

            allValues
        }
    }

    override suspend fun save(entity: String) {
        super.save(entity.lowercase())
    }

    override suspend fun get(id: String): String? {
        return super.get(id.lowercase())
    }

    override suspend fun delete(id: String) {
        super.delete(id.lowercase())
    }
}