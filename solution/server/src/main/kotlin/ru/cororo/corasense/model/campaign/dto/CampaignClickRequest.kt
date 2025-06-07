package ru.cororo.corasense.model.campaign.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.util.UuidString

@Serializable
data class CampaignClickRequest(
    @SerialName("client_id")
    val clientId: UuidString
)