package ru.cororo.corasense.service

import io.micrometer.core.instrument.MeterRegistry
import ru.cororo.corasense.shared.model.action.AdActionStats
import ru.cororo.corasense.shared.model.action.AdActionStatsDaily
import ru.cororo.corasense.shared.model.advertiser.Advertiser
import ru.cororo.corasense.shared.model.campaign.Campaign
import java.util.*

interface MicrometerService {
    fun init(meterRegistry: MeterRegistry)

    fun updateCampaignStats(campaign: Campaign, stats: AdActionStats)

    fun updateAdvertiserStats(advertiser: Advertiser, stats: AdActionStats)

    fun updateDailyCampaignStats(campaign: Campaign, stats: AdActionStatsDaily)

    fun updateDailyAdvertiserStats(advertiser: Advertiser, stats: AdActionStatsDaily)

    suspend fun markToUpdateCampaign(campaignId: UUID)

    suspend fun markToUpdateAdvertiser(advertiserId: UUID)

    suspend fun deleteCampaignStats(campaignId: UUID)

    suspend fun deleteAdvertiserStats(advertiserId: UUID)
}