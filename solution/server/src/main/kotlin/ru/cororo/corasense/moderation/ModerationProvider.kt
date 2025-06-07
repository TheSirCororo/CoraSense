package ru.cororo.corasense.moderation

import ru.cororo.corasense.shared.model.moderation.ModerationVerdict

interface ModerationProvider {
    suspend fun moderate(text: String): ModerationVerdict
}