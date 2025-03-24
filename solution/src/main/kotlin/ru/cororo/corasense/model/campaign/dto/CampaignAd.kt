package ru.cororo.corasense.model.campaign.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.util.UuidString

@Serializable
data class CampaignAd(
    @SerialName("ad_id")
    val adId: UuidString,
    @SerialName("ad_title")
    val adTitle: String,
    @SerialName("ad_text")
    val adText: String,
    @SerialName("advertiser_id")
    val advertiserId: UuidString,
    @SerialName("image_id")
    val imageId: UuidString? = null
)