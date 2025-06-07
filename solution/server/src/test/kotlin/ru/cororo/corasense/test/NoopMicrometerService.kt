package ru.cororo.corasense.test

import io.micrometer.core.instrument.MeterRegistry
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.service.MicrometerService
import java.util.UUID

object NoopMicrometerService : MicrometerService {
    override fun init(meterRegistry: MeterRegistry) {}

    override fun updateCampaignStats(
        campaign: Campaign,
        stats: AdActionStats
    ) {}

    override fun updateAdvertiserStats(
        advertiser: Advertiser,
        stats: AdActionStats
    ) {}

    override fun updateDailyCampaignStats(
        campaign: Campaign,
        stats: AdActionStatsDaily
    ) {}

    override fun updateDailyAdvertiserStats(
        advertiser: Advertiser,
        stats: AdActionStatsDaily
    ) {}

    override suspend fun markToUpdateCampaign(campaignId: UUID) {}

    override suspend fun markToUpdateAdvertiser(advertiserId: UUID) {}
    override suspend fun deleteCampaignStats(campaignId: UUID) {}

    override suspend fun deleteAdvertiserStats(advertiserId: UUID) {}
}