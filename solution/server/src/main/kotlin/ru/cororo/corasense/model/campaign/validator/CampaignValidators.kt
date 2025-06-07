package ru.cororo.corasense.model.campaign.validator

import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import ru.cororo.corasense.shared.model.campaign.CampaignCreateData
import ru.cororo.corasense.shared.model.campaign.CampaignCreateData.Targeting
import ru.cororo.corasense.validation.validator

fun campaignCreateDataValidator() = validator<CampaignCreateData> {
    CampaignCreateData::costPerImpression {
        minimum(0)
    }

    CampaignCreateData::costPerClick {
        minimum(0)
    }

    CampaignCreateData::clicksLimit {
        minimum(0)
    }

    CampaignCreateData::impressionsLimit {
        minimum(0)
    }

    CampaignCreateData::adTitle {
        minLength(1)
        maxLength(256)
    }

    CampaignCreateData::adText {
        minLength(1)
    }

    constrain("endDate >= startDate") {
        it.endDate >= it.startDate
    }

    CampaignCreateData::targeting ifPresent {
        Targeting::ageTo ifPresent {
            minimum(0)
        }

        Targeting::ageFrom ifPresent {
            minimum(0)
        }

        constrain("ageTo >= ageFrom") {
            if (it.ageTo != null && it.ageFrom != null) it.ageTo!! >= it.ageFrom!!
            else true
        }
    }
}