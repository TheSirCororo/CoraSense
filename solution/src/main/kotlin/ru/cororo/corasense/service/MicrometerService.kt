package ru.cororo.corasense.service

import io.micrometer.core.instrument.MeterRegistry
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.model.campaign.data.Campaign
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