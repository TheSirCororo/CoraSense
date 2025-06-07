package ru.cororo.corasense.shared.model.advertiser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.shared.util.UuidString

@Serializable
data class Advertiser(
    @SerialName("advertiser_id")
    val id: UuidString,
    val name: String
)