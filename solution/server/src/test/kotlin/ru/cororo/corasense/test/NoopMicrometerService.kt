package ru.cororo.corasense.test

import io.micrometer.core.instrument.MeterRegistry
import ru.cororo.corasense.shared.model.action.AdActionStats
import ru.cororo.corasense.shared.model.action.AdActionStatsDaily
import ru.cororo.corasense.shared.model.advertiser.Advertiser
import ru.cororo.corasense.shared.model.campaign.Campaign
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