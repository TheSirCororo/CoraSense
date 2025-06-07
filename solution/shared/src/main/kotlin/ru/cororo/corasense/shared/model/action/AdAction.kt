package ru.cororo.corasense.shared.model.action

import kotlinx.serialization.Serializable
import ru.cororo.corasense.shared.util.UuidString

@Serializable
data class AdAction(
    val id: UuidString,
    val advertiserId: UuidString,
    val campaignId: UuidString,
    val clientId: UuidString,
    val type: Type,
    val day: Int,
    val cost: Double
) {
    enum class Type {
        CLICK,
        IMPRESSION
    }
}
