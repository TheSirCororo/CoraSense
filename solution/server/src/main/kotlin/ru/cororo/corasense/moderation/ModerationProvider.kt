package ru.cororo.corasense.moderation

import ru.cororo.corasense.model.moderation.data.ModerationVerdict

interface ModerationProvider {
    suspend fun moderate(text: String): ModerationVerdict
}