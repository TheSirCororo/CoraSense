package ru.cororo.corasense.model.moderation.dto

import kotlinx.serialization.Serializable

@Serializable
data class BlockedWordsRequest(
    val words: Collection<String>
)
