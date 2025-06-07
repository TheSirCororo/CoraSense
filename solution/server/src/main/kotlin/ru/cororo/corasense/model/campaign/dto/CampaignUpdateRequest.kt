package ru.cororo.corasense.model.campaign.dto

import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest.Targeting
import ru.cororo.corasense.util.UuidString
import ru.cororo.corasense.validation.validator

@Serializable
data class CampaignUpdateRequest(
    @SerialName("cost_per_impression")
    val costPerImpression: Double,
    @SerialName("cost_per_click")
    val costPerClick: Double,
    @SerialName("ad_title")
    val adTitle: String,
    @SerialName("ad_text")
    val adText: String,
    @SerialName("impressions_limit")
    val impressionsLimit: Int,
    @SerialName("clicks_limit")
    val clicksLimit: Int,
    @SerialName("start_date")
    val startDate: Int,
    @SerialName("end_date")
    val endDate: Int,
    @SerialName("image_id")
    val imageId: UuidString? = null,
    val targeting: Targeting? = null
) {
    companion object {
        init {
            validator<CampaignUpdateRequest> {
                CampaignUpdateRequest::costPerImpression {
                    minimum(0)
                }

                CampaignUpdateRequest::costPerClick {
                    minimum(0)
                }

                CampaignUpdateRequest::adTitle {
                    minLength(1)
                    maxLength(256)
                }

                CampaignUpdateRequest::adText {
                    minLength(1)
                }

                CampaignUpdateRequest::clicksLimit {
                    minimum(0)
                }

                CampaignUpdateRequest::impressionsLimit {
                    minimum(0)
                }

                CampaignUpdateRequest::targeting ifPresent {
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