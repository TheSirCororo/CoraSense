package ru.cororo.corasense.shared.model.campaign

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.shared.util.UuidString

@Serializable
data class CampaignCreateData(
    @SerialName("impressions_limit")
    val impressionsLimit: Int,
    @SerialName("clicks_limit")
    val clicksLimit: Int,
    @SerialName("cost_per_impression")
    val costPerImpression: Double,
    @SerialName("cost_per_click")
    val costPerClick: Double,
    @SerialName("ad_title")
    val adTitle: String,
    @SerialName("ad_text")
    val adText: String,
    @SerialName("start_date")
    val startDate: Int,
    @SerialName("end_date")
    val endDate: Int,
    @SerialName("image_id")
    val imageId: UuidString? = null,
    val targeting: Targeting? = null
) {
    @Serializable
    data class Targeting(
        val gender: Campaign.Targeting.Gender? = null,
        @SerialName("age_from")
        val ageFrom: Int? = null,
        @SerialName("age_to")
        val ageTo: Int? = null,
        val location: String? = null
    )
}