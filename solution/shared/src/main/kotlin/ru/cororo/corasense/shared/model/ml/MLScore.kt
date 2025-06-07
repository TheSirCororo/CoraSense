package ru.cororo.corasense.shared.model.ml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.shared.util.UuidString

@Serializable
data class MLScore(
    @SerialName("client_id")
    val clientId: UuidString,
    @SerialName("advertiser_id")
    val advertiserId: UuidString,
    val score: Int
)