package ru.cororo.corasense.model.ml.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.util.UuidString

@Serializable
data class MLScore(
    @SerialName("client_id")
    val clientId: UuidString,
    @SerialName("advertiser_id")
    val advertiserId: UuidString,
    val score: Int
)
