package ru.cororo.corasense.model.campaign.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.service.CurrentDayService
import ru.cororo.corasense.util.UuidString

@Serializable
data class Campaign(
    @SerialName("campaign_id")
    val id: UuidString,
    @SerialName("advertiser_id")
    val advertiserId: UuidString,
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
    val targeting: Targeting
) {
    @Serializable
    data class Targeting(
        val gender: Gender? = null,
        @SerialName("age_from")
        val ageFrom: Int? = null,
        @SerialName("age_to")
        val ageTo: Int? = null,
        val location: String? = null
    ) {
        enum class Gender {
            ALL,
            FEMALE,
            MALE
        }
    }
}

suspend fun Campaign.hasStarted(currentDayService: CurrentDayService) = currentDayService.getCurrentDay() >= startDate

suspend fun Campaign.hasEnded(currentDayService: CurrentDayService) = currentDayService.getCurrentDay() > endDate
