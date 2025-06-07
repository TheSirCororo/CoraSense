package ru.cororo.corasense.shared.model.moderation

import kotlinx.serialization.Serializable

@Serializable
data class ModerationVerdict(
    val reason: String?,
    val allowed: Boolean
) {
    companion object {
        val Allowed = ModerationVerdict(null, true)
    }
}