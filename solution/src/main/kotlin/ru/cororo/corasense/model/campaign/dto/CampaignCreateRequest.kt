package ru.cororo.corasense.model.campaign.dto

import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.model.campaign.data.Campaign.Targeting.Gender
import ru.cororo.corasense.util.UuidString
import ru.cororo.corasense.validation.validator

@Serializable
data class CampaignCreateRequest(
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
        val gender: Gender? = null,
        @SerialName("age_from")
        val ageFrom: Int? = null,
        @SerialName("age_to")
        val ageTo: Int? = null,
        val location: String? = null
    )

    companion object {
        init {
            validator<CampaignCreateRequest> {
                CampaignCreateRequest::costPerImpression {
                    minimum(0)
                }

                CampaignCreateRequest::costPerClick {
                    minimum(0)
                }

                CampaignCreateRequest::clicksLimit {
                    minimum(0)
                }

                CampaignCreateRequest::impressionsLimit {
                    minimum(0)
                }

                CampaignCreateRequest::adTitle {
                    minLength(1)
                    maxLength(256)
                }

                CampaignCreateRequest::adText {
                    minLength(1)
                }

                constrain("endDate >= startDate") {
                    it.endDate >= it.startDate
                }

                CampaignCreateRequest::targeting ifPresent {
                    Targeting::ageTo ifPresent {
                        minimum(0)
                    }

                    Targeting::ageFrom ifPresent {
                        minimum(0)
                    }

                    constrain("ageTo >= ageFrom") {
                        if (it.ageTo != null && it.ageFrom != null) it.ageTo >= it.ageFrom
                        else true
                    }
                }
            }
        }
    }
}
