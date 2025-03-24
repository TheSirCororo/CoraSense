package ru.cororo.corasense.repo.action

import org.jetbrains.exposed.dao.id.UUIDTable
import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.repo.advertiser.AdvertiserTable
import ru.cororo.corasense.repo.campaign.CampaignTable
import ru.cororo.corasense.repo.client.ClientTable

object AdActionTable : UUIDTable("ad_actions") {
    val advertiserId = reference("advertiser_id", AdvertiserTable)
    val campaignId = reference("campaign_id", CampaignTable)
    val clientId = reference("client_id", ClientTable)
    val type = enumerationByName<AdAction.Type>("type", 10)
    val day = integer("day")
    val cost = double("cost")

    init {
        uniqueIndex("ad_action_unique", campaignId, clientId, type)
    }
}