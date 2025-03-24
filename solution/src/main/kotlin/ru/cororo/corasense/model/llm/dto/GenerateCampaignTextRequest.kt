package ru.cororo.corasense.model.llm.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.util.UuidString

@Serializable
data class GenerateCampaignTextRequest(
    @SerialName("advertiser_id")
    val advertiserId: UuidString,
    @SerialName("campaign_title")
    val campaignTitle: String
)
