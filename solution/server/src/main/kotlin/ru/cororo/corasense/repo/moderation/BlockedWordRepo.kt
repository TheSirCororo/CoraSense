package ru.cororo.corasense.repo.moderation

import ru.cororo.corasense.repo.CrudRepo

interface BlockedWordRepo : CrudRepo<String, String> {
    suspend fun getAll(): Set<String>
}