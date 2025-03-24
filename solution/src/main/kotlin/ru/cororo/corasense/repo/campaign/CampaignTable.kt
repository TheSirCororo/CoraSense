package ru.cororo.corasense.repo.campaign

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.json.json
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.repo.advertiser.AdvertiserTable
import ru.cororo.corasense.repo.image.ImageTable

object CampaignTable : UUIDTable("campaigns") {
    val advertiserId = reference("advertiser_id", AdvertiserTable)
    val impressionsLimit = integer("impressions_limit")
    val clicksLimit = integer("clicks_limit")
    val costPerImpression = double("cost_per_impression")
    val costPerClick = double("cost_per_click")
    val adTitle = varchar("ad_title", 256)
    val adText = largeText("ad_text")
    val startDate = integer("start_date")
    val endDate = integer("end_date")
    val imageId = reference("file_id", ImageTable).nullable().default(null)
    val targeting = json<Campaign.Targeting>("targeting", Json)
}